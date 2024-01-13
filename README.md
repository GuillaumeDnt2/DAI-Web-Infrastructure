# DAI-Web-Infrastructure

## Server web Ngninx

### Dockerfile
Pour créer notre image docker, nous avons repris la dernière image officielle de nginx et copié notre site web dans le dossier /www et le fichier de configuration nginx.

### nginx.conf
Notre serveur nginx est configurer pour écouter toutes les connexion sur le port 80 (0.0.0.0:80) et aussi configurer la racine de notre serveur dans le dossier /www (le dossier contenant notre site web). Notre site web ne comporte que une seul page et donc nous avons une seul location avec notre fichier index.html.

### Site web statique
Nous avons choisi une template sur startboostrap.com et légérement modifié sont contenu pour en faire un site web simple avec quelque boutons et un menu.
#### Image docker 
Contenu du dockerfile de l'image du site web statique:
```
FROM nginx:latest
COPY ./site/ /www
COPY ./nginx.conf /etc/nginx/conf.d/default.conf
```
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
Maintenant pour faire une API HTTP nous avons développé un petit programme Java utilisant la librairie Javalin. 
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

### Configuration de l'image du server web
```
webserver:
    build:
      context: ./StaticWebServer
    expose:
      - "80"  
    deploy:
      replicas: 1
    labels:
      - traefik.http.routers.webserver.rule=Host(`localhost`)
```
La commande .rule=Host('localhost') permet de configurer Traefik pour qu'il redirige cet url vers la serveur web. Donc pour accèder au site web il faut utiliser http:localhost.

### Configuration de l'image de l'API
```
 api:
    build:
      context: ./HttpAPI
    expose:
      - "3141"  
    deploy:
      replicas: 1
    labels:
      - traefik.http.routers.api.rule=Host(`localhost`) && PathPrefix(`/api/`)
```
Il faut spécifier un chemin supplémentaire pour que Traefik redirige le chemin "localhost/api/" sur l'image Docker de l'API.
On ne doit pas spécifier le port pour accéder à l'API (par ex. "localhost:3141") sinon ça n'arrivera pas sur Traefik. On doit seulement utiliser le chemin configuré pour Traefik pour qu'il puisse le reconnaitre et nous rediriger vers l'API.


## Load Balancing
Commande pour ajouter des nouvelles instances d'un service : ```docker compose up --scale <service>=nbInstance --no-recreate```
Ajouter dans le docker compose le paramètre :
```
  deploy:
      replicas: 3
```
