# DAI-Web-Infrastructure
## Présentation
Ce projet consiste en une infrastructure web implémentée à l'aide de Docker Compose. Cette infrastructure est composée d'un ou plusieurs serveurs web statique, une API CRUD HTTP avec sa base de donnée Postgresql ainsi qu'un reverse proxy Traefik. Ce qui suit explique comment nous avons construit cette infrastructure dans l'ordre dans laquelle nous l'avons réalisée.

## Server web Ngninx

### Dockerfile
Pour créer notre image docker, nous avons repris la dernière image officielle de nginx et copié notre site web dans le dossier /www et le fichier de configuration nginx.

#### nginx.conf
```
server{
    listen 0.0.0.0:80;
    root /www;

    location / {
        index index.html;
    }

}
```
Notre serveur nginx est configuré pour écouter toutes les connexion sur le port 80 (0.0.0.0:80) et aussi configurer la racine de notre serveur dans le dossier /www (le dossier contenant notre site web). Notre site web ne comporte que une seul page et donc nous avons une seul location avec notre fichier index.html.

### Site web statique
Nous avons choisi une template sur startboostrap.com et avons légérement modifié sont contenu pour en faire un site web simple avec quelque boutons et un menu.
#### Image docker 
Contenu du dockerfile de l'image du site web statique:
```
FROM nginx:latest
COPY ./site/ /www
COPY ./nginx.conf /etc/nginx/conf.d/default.conf
```
Dans ce dockerfile on monte le répertoire principal du site et on indique aussi le fichier de configuration de Nginx.
### Configuration docker compose
```
services:
  webserver:
    image: web/static
    ports:
      - "8080:80"
``` 
Pour l'instant docker compose va démarrer l'image du site web statique et bind le port 80 sur 8080.

## API CRUD HTTP
Pour faire une API HTTP nous avons développé un petit programme Java utilisant la librairie Javalin. 
Cette API nous permet de gérer une liste de pays ainsi que leurs informations (capitales et population). 
L'API propose toutes les opérations CRUD (Create-Read-Update-Delete).
Pour correctement utiliser l'API il est conseillé de faire les commandes avec un client API (comme [Bruno](https://www.usebruno.com/)). 

## Package Java avec Maven
Pour faire un nouveau package Java de cet API nous devons ajouter une dépendance à Javalin dans le fichier pom.xml :
```
<dependencies>
        <dependency>
            <groupId>io.javalin</groupId>
            <artifactId>javalin-bundle</artifactId>
            <version>5.6.3</version>
        </dependency>
</dependencies>
```


### Exemples de commandes avec Bruno :
Ajout d'un pays :

<img width="355" alt="image" src="https://github.com/GuillaumeDnt2/DAI-Web-Infrastructure/assets/113915093/56c62a4b-100f-48c1-b06b-b1792bdd28c8"> 

Suppression d'un pays :

<img width="343" alt="image" src="https://github.com/GuillaumeDnt2/DAI-Web-Infrastructure/assets/113915093/c86319f4-ab1a-4dde-93eb-364a33f8784d"> 

Modification d'un pays :

<img width="341" alt="image" src="https://github.com/GuillaumeDnt2/DAI-Web-Infrastructure/assets/113915093/d9a077dd-1d7b-4ea7-ab31-f270265967fd"> 

Affichage d'un pays :

<img width="343" alt="image" src="https://github.com/GuillaumeDnt2/DAI-Web-Infrastructure/assets/113915093/c82d5350-e101-466b-81b1-33a18c6f7fe8"> 

Affichage de tous les pays :

<img width="350" alt="image" src="https://github.com/GuillaumeDnt2/DAI-Web-Infrastructure/assets/113915093/3168b836-ecac-4a78-9d4e-162a86b62ff4"> 

Exemple de résultat avec la commande GET ci dessus :
```
{
  "Switzerland": {
    "name": "Switzerland",
    "capital": "Lausanne",
    "population": 8796669,
    "flagPath": null
  },
  "Germany": {
    "name": "Germany",
    "capital": "Berlin",
    "population": 832994633,
    "flagPath": null
  },
  "India": {
    "name": "India",
    "capital": "New Delhi",
    "population": 1428627663,
    "flagPath": null
  }
}
```

## Reverse proxy avec Traefik
### Modifications du fichier docker compose
#### Configuration de l'image Traefik
```
 traefik:
    image: traefik
    command:
      - --api.insecure=true
      - --providers.docker
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
    ports:
      - "80:80"
      - "3141:3141"
      - "8080:8080"
```

Nous utilisons le port par défaut pour le site web, le port 3141 toujours pour l'API et le port 8080 pour le dashboard Traefik.
> [!NOTE]
> Il faut monter un volume de Traefik sur le socket Docker pour qu'il ait accès aux containers

### Configuration de l'image du server web
```
webserver:
    build:
      context: ./StaticWebServer
    expose:
      - "80"  
    labels:
      - traefik.http.routers.webserver.rule=Host(`localhost`)
```
La commande `.rule=Host('localhost')` permet de configurer Traefik pour qu'il redirige cet url vers la serveur web. Donc pour accèder au site web il faut utiliser l'url "http:localhost".

### Configuration de l'image de l'API
```
 api:
    build:
      context: ./HttpAPI
    expose:
      - "3141"  
    labels:
      - traefik.http.routers.api.rule=Host(`localhost`) && PathPrefix(`/api/`)
```
Il faut spécifier un chemin supplémentaire pour que Traefik redirige le chemin `localhost/api/` sur le container de l'API.
On ne doit pas spécifier le port pour accéder à l'API (par ex. "localhost:3141") sinon ça n'arrivera pas sur Traefik. On doit seulement utiliser le chemin configuré pour Traefik pour qu'il puisse le reconnaitre et nous rediriger vers l'API.
> [!IMPORTANT]
> Il faut modifier les urls d'accès dans l'API car maintenant le chemin de base de l'api est `localhost/api` et plus juste `localhost`


## Load Balancing
Pour que le container Docker Compose accueille plusieurs instances de chaque service, il faut ajouter ce paramètre à chaque service :
```
  deploy:
      replicas: 3
```
On peut aussi ajouter des instances au container avec la commande : ```docker compose up --scale <service>=nbInstance --no-recreate```. 
Le petit désavantage de cette commande est qu'elle va bien créer n instance du service mais cela va réinitialiser le nombre d'instances des autres services au nombre spécifié dans le paramètre deploy du fichier compose.yml. 

Pour pallier à ce problème il faut spécifier les deux services `api` et `webserver` dans la commande :
```docker compose up --scale api=3 --scale webserver=4 --no-recreate```
Il est aussi possible de d'abbord lancer traefik tout seul avec la commande ```docker compose up traefik``` et par la suite lancer le nombre d'instance qu'on veut pour chaques services.

> [!NOTE] 
> C'est comme ça qu'on peut ajuster dynamiquement le nombre de serveurs gérés par load balancing sans arrêter le container Docker.

## Sticky sessions et round robin
La méthode de load balancing qu'utilise Traefik est déjà du round robin, il ne faut rien rajouter dans la configuration.
Pour les sticky sessions, nous voulons que les clients communiquent avec l'API sur la même instance à chaque fois. Il nous faut alors activer les sessions persistantes sur Traefik.

### Modification de compose.yml
Il faut ajouter deux nouvelles lignes dans les labels de l'API :
```
 - traefik.http.services.api.loadbalancer.sticky.cookie=true
 - traefik.http.services.api.loadbalancer.sticky.cookie.name=RouteID
```
La 1ère ligne indique qu'on active les sticky sessions, avec Traefik les sticky sessions sont réalisées par des cookies ajoutés dans les headers HTTP.
La 2e ligne indique le nom du cookie et il ne reste plus rien d'autre à faire, Traefik s'occupe de tout.

### Logs avec Traefik
Afin de prouver que les sticky sessions fonctionnent bien, nous pouvons activer les logs d'accès de Traefik.
Il suffit d'ajouter le paramètre suivant dans le fichier Traefik.yaml :
```
accessLog:
  filePath: "/logs/access.log"

```
Maintenant chaque connection passant par le reverse proxy sera affichée dans ce fichier de log. 

#### Preuve des sticky sessions

**Accès sur le site web statique**
```
172.22.0.1 - - [17/Jan/2024:16:48:14 +0000] "GET / HTTP/2.0" 200 9618 "-" "-" 67 "webserver@docker" "http://172.22.0.3:80" 0ms
172.22.0.1 - - [17/Jan/2024:16:49:05 +0000] "GET / HTTP/2.0" 200 9618 "-" "-" 71 "webserver@docker" "http://172.22.0.8:80" 1ms
172.22.0.1 - - [17/Jan/2024:16:49:06 +0000] "GET / HTTP/2.0" 200 9618 "-" "-" 75 "webserver@docker" "http://172.22.0.11:80" 0ms
```
Comme les sticky sessions ne sont pas activées pour le serveur web, quand on se connecte plusieurs fois sur le site web le reverse proxy va rediriger la requête sur des serveurs différents.
L'adresse IP du serveur de destination est différente pour chaque requête.


**Accès sur l'API**
```
172.22.0.1 - - [17/Jan/2024:16:45:50 +0000] "GET /api/country HTTP/2.0" 200 262 "-" "-" 24 "api@docker" "http://172.22.0.2:3141" 6ms
172.22.0.1 - - [17/Jan/2024:16:45:51 +0000] "GET /api/country HTTP/2.0" 200 262 "-" "-" 25 "api@docker" "http://172.22.0.2:3141" 2ms
172.22.0.1 - - [17/Jan/2024:16:46:01 +0000] "GET /api/country HTTP/2.0" 200 262 "-" "-" 26 "api@docker" "http://172.22.0.2:3141" 2ms
172.22.0.1 - - [17/Jan/2024:16:46:01 +0000] "GET /api/country HTTP/2.0" 200 262 "-" "-" 27 "api@docker" "http://172.22.0.2:3141" 1ms
```
Les sticky sessions par cookie sont maintenant activées pour l'API HTTP. Un client est censé converser avec la même instance du serveur HTTP.
On remarque dans les logs que l'adresse IP de destination n'a pas changée, donc la requête HTTP a toujours été redirigée vers le même serveur. 

## Sécurisation TLS

## Interface de gestion de l'infrastructure
Nous avons décidé d'utiliser une solution existante pour gérer une infrastructure sur Docker.
Nous allons utiliser `Portainer`.

### Portainer
Portainer est un outil de gestion d'environnements Docker ou Kubernetes, il propose une web app qui permet entre autre de créer, modifier ou supprimer des containers au sein d'un environnement créé avec Docker Compose par exemple.

#### Déploiement de Portainer
Portainer est une image Docker qui doit s'exécuter dans une stack différente de l'infrastructure web. Pour cela il faut faire un nouveau fichier Docker compose :
```
version: "3"
services:
  portainer:
    image: portainer/portainer-ce:latest
    ports:
      - 9443:9443
    volumes:
      - data:/data
      - /var/run/docker.sock:/var/run/docker.sock
    restart: unless-stopped
volumes:
  data:
```
Portainer a besoin d'avoir accès au socket Docker afin qu'il puisse y faire des lectures et des modifications.
Ensuite il suffit simplement de lancer notre infrastructure en ligne de commande avec ```docker compose up``` dans le réportoire où se trouve le fichier `compose.yml`.
Portainer est par défaut accessible depuis le chemin ```https://localhost:9443```.
#### Interface de Portainer
> [!NOTE] 
> Avant de commencer à utiliser Portainer, on est invité à créer un compte administrateur Portainer qui est local au container Docker.

Contenu de l'onglet `stack` :

<img width="646" alt="image" src="https://github.com/GuillaumeDnt2/DAI-Web-Infrastructure/assets/113915093/f9f2b997-4984-48ec-97ab-848119167aaa">

À ce stade il existe deux stacks dans Docker, une pour l'infrastructure web et une autre pour Portainer seul.
En sélectionnant la stack de l'infrastructure web nous pouvons voir la liste des containers qu'elle contient :

<img width="661" alt="image" src="https://github.com/GuillaumeDnt2/DAI-Web-Infrastructure/assets/113915093/640a0e72-c4ea-40d8-b5f9-e51594e2ad5e">


Depuis ici on peut effectuer plusieures actions sur les containers, les arrêter, les supprimer ou les modifier.
#### Ajouter plus de serveur
Depuis la page d'un container on peut sélectionner l'option `Duplicate/Edit` :

<img width="549" alt="image" src="https://github.com/GuillaumeDnt2/DAI-Web-Infrastructure/assets/113915093/f29a4113-93ac-4a9c-a042-cfb29a264ecd">

Ce qui nous permet ensuite de créer une copie de ce container si on change le nom.

> [!TIP]
> Il ne faut pas oublier d'exposer le port correspondant au service sur le container. 3141 pour l'API et 80 pour le web statique
> <img width="816" alt="image" src="https://github.com/GuillaumeDnt2/DAI-Web-Infrastructure/assets/113915093/f59f52cc-bb40-496b-8f26-c431f79ff2c6">

Pour terminer il faut sélectionner `Deploy the container` et une nouvelle instance de notre serveur sera en ligne et Traefik le reconnaitra directement.




