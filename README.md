# thumbnail-service
A basic web service to compute jpg thumbnails.     

## Testing
    
The project is based on maven thus to run tests form the root directory run 

    mvn clean test

## Creating the docker image
The docker image can be generated with the already present maven configuration with the following command

    mvnw spring-boot:build-image -Dspring-boot.build-image.imageName=luigidb/thumbnail-service -DskipTests

The local docker image can be uploaded to docker hub with

    docker login –u luigidb
    docker push luigidb/thumbnail-service:latest

## Run the service
With maven run 

    mvn spring-boot:run

With the "official" docker image   

    docker run –p 8080:8080 –t luigidb/thumbnail-service:latest

## Update Docker image  

Just for reference the local docker image can be uploaded to docker hub with  
    
    docker login –u luigidb
    docker push luigidb/thumbnail-service:latest

## API endpoints

![enpoints](apiEndpoint.png)

### /thumbnails
This one is the main entry point and is used to load images to the service.
Support only POST method and answers with 202 (ACCEPTED) 

Usage:

    curl --location --request POST 'localhost:8080/thumbnails' --form 'file=@"/E:/orange.jpg"'

The response contains also useful links to the subsequent necessary api to download the thumbnail and to monitor 
the status of the operation.

Example reply from the provided usage:
```
{
    "_links": {
        "thumbnail": {
            "href": "http://localhost:8080/thumbnails/thumbnail_orange.jpg/result"
        },
        "status": {
            "href": "http://localhost:8080/thumbnails/orange.jpg"
        }
    }
}
```
### /thumbnails/{filename}
This endpoint monitor the processing of the thumbnails. 
Support only the GET method and can answer with 
* 303 (REDIRECT) If the thumbnail has already been created 
* 404 (NOT FOUND) If the thumbnail is still in the processing

Usage:

    curl --location --request GET 'localhost:8080/thumbnails/orange.jpg'

In case of 303 reply embedded in the response there is a link to the api needed to download the thumbnail.
```
{
    "_links": {
        "thumbnail": {
            "href": "http://localhost:8080/thumbnails/thumbnail_orange.jpg/result"
        }
    }
}
```

### /thumbnails/{filename}/result
This endpoint is the one needed to retrieve and delete the thumbnails.
Support GET and DELETE methods and can answer with
* GET 
  * 200 (OK) with the thumbnail in the body 
  * 404 (NOT FOUND) If the thumbnail cannot be found
* DELETE 
  * 202 (ACCEPTED)

Usage:

    curl --location --request GET 'localhost:8080/thumbnails/thumbnail_orange.jpg/result'

    curl --location --request DELETE 'localhost:8080/thumbnails/thumbnail_orange.jpg/result'


## Architecture

## Future expansions
