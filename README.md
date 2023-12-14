# DAI-Web-Infrastructure

## Server web Ngninx
### Site web statique
Nous avons choisi une template sur startboostrap.com
### Configuration docker compose
```
services:
  webserver:
    image: web/static
    ports:
      - "8080:80"
``` 
