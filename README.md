# web-chat-demo

## Project Overview

The `web-chat-demo` is a monolithic web application developed for demonstrational purposes. It features a backend written in Java using Quarkus, and a frontend built with React.js. The application is designed for deployment on a single EC2 instance, incorporating both REST endpoints and a WebSocket server alongside the React frontend. While this architecture is not optimal for scalable chat applications, it serves its purpose as a proof-of-concept.

### Technologies Used
- **Backend**: Java (JDK 17+), Quarkus
- **Frontend**: React.js
- **Database**: DynamoDB
- **Deployment**: Docker, AWS (EC2), AWS SAM

## Environment Requirements
- **Java**: JDK 17+
- **Maven**: Latest stable version
- **Docker**: Latest stable version
- **AWS SAM CLI**: Required for AWS deployment
- **AWS CLI**: Recommended for managing AWS resources
- **npm**: v10+

## Building the Project
The project uses Maven for building, and the resulting application runs within Docker containers. The provided scripts also publish the resulting Docker image to Docker Hub. Therefore, you need to set the `dockerhub.username` property in the root `pom.xml`. Before proceeding with the build, make sure to log in to Docker with:
```sh
docker login
```
Then, use the following commands to build and deploy the Docker image to Docker Hub:
```sh
mvn deploy
```
After the build is complete, verify that the Docker Hub contains the application's image.

## Deployment Scenarios
The application supports multiple deployment modes:

### 1. Running Quarkus in Dev Mode
1. Navigate to the backend directory.
2. Set `quarkus.http.port=8081` in `application.properties`.
3. Run the command:
   ```sh
   mvn quarkus:dev -Dquarkus.http.host=0.0.0.0
   ```
4. Navigate to the frontend directory.
5. Run:
   ```sh
   npm install
   npm start
   ```

### 2. Running with Docker Compose
1. Ensure the line `quarkus.http.port=8081` is commented out in `application.properties`.
2. From the root directory, run:
   ```sh
   docker-compose up -d
   ```
3. View logs with:
   ```sh
   docker-compose logs -f > docker-compose.log
   ```
4. To stop the containers, run:
   ```sh
   docker-compose down
   ```
5. If you made changes to the code, rebuild the application image with:
   ```sh
   docker-compose build backend
   docker-compose up -d
   docker-compose logs -f > docker-compose.log
   ```
6. The application will be available at [http://localhost:8080](http://localhost:8080).

### 3. Deployment on AWS
The deployment configuration is located in the `.deployment` directory. This directory contains an AWS SAM (Serverless Application Model) template (`template.yaml`) that defines the resources required for the deployment, including:

- **EC2 Instance**: Hosts the monolithic `web-chat-demo` application.
- **Security Group**: Configures the necessary network settings.
- **Instance Role**: Grants the EC2 instance permissions to interact with other AWS services, such as DynamoDB.

The application is deployed on a single EC2 instance, which is provisioned along with the necessary Security Groups and Instance Role. The `web-chat-demo` application will automatically create the required DynamoDB table structure during its first run. The deployment uses the default VPC and assigns a public IP address to the EC2 instance. After deployment, the application is accessible via the EC2 instance's public IP address on port 8080. This setup is designed to fit within the AWS Free Tier limitations, and therefore, all configurations are optimized for minimal cost.

After completing your experiments, remember to delete all resources, including the DynamoDB tables, as they are not automatically removed by the deployment script.

#### Prerequisites
Before deploying the application, complete the following preliminary steps:

1. **AWS Account**: If you do not already have an AWS account, register at [https://aws.amazon.com](https://aws.amazon.com).
2. **AWS Credentials**: Obtain your `aws_access_key_id` and `aws_secret_access_key` from the AWS Management Console and save them in the `.aws/credentials` file. Detailed instructions are provided in `.deployment/aws_credentials_instruction.md`.
3. **EC2 Key Pair**: It is recommended to have an EC2 Key Pair for SSH access to the instance. You can create one using the AWS Management Console. Detailed instructions are available in `.deployment/keypair_instruction.md`.
4. **Docker Image**: The application is deployed as a Docker container, requiring the image to be available in a public Docker registry. A public repository has been created for this project, but the image might be removed after a few weeks. You can use your own Docker Hub repository by building and publishing the image as described in the "Building the Project" section. If using your own repository, edit the `UserData` section in the `WebChatDemoEC2Instance` module in `template.yaml` to replace `avchesnokov/web-chat-demo` with your own repository.

#### Deployment Steps
After completing the preliminary steps, run the following command to deploy the application:

```sh
sam deploy --stack-name webchat-demo --region eu-central-1 --capabilities CAPABILITY_IAM --parameter-overrides KeyName=<your KeyPair name> InstanceType=<EC2 instance type> AwsAmi=<your AMI> VpcId=<your VPC ID> DockerImageTag=<your docker image tag>
```

Where:
- `<your KeyPair name>`: The name of the Key Pair created in the AWS console.
- `<EC2 instance type>`: The desired EC2 instance type. Acceptable values are `t2.micro`, `t3.micro`, `t3.small`. The default is `t3.micro`. You can omit this parameter if the default is acceptable.
- `<your AMI>`: The Amazon Machine Image (AMI) to use for the EC2 instance. It is recommended to use the "Amazon Linux 2 AMI (HVM)". The default value is set for the `eu-central-1` region. If you are using a different region, you must specify the appropriate AMI ID.
- `<your VPC ID>`: The ID of the VPC where you want to deploy the application. It must have public access.
- `<your docker image tag>`: The tag of the Docker image in your Docker Hub repository. The default is `latest`, which is a good choice if you do not need a specific version.

After the deployment is complete, copy the service URL from the output log.

## Considerations and Limitations

1. **Design Philosophy**: The architecture of the application was driven by an absolute minimalism principle. Due to unexpected time constraints over the last three weeks, I had to focus on simplicity and avoid complex implementations.

2. **AWS Free Tier Compatibility**: One of my key priorities was to ensure the application could be deployed using the AWS Free Tier, avoiding any costs for evaluators. This approach influenced several design decisions, such as avoiding the use of public domains and TLS certificates.

3. **Deviation from Common WebSocket Architecture**: Instead of using the traditional setup for such services, where API Gateway routes WebSocket traffic to AWS Lambda or an EC2-based REST API, I decided to roll my own WebSocket server. Honestly, part of it was just the desire to do something different—call it a bit of "developer mischief." Sure, setting up API Gateway and Lambda would have solved my HTTPS woes, but I felt like getting my hands dirty and making things my own way. Originally, I wanted to split the WebSocket server into its own app and use CloudFront to handle the HTTPS part, but I ran out of time. It remains an idea for future iterations!

4. **Quarkus Instead of Spring**: The choice of Quarkus over the Spring Framework was motivated by pure curiosity and a bit of an experimental spirit. My initial plan was to compile the application to a native image, but my laptop clearly wasn't up for the challenge (hello, insufficient memory!). I spent way too much time trying to get it working, but hey, it was fun to try, and I learned a lot about what Quarkus is capable of.

5. **UI Design Limitations**: Look, I'm the first to admit—I'm no designer. The UI is pretty basic, and my JavaScript and React skills aren't what I'd call "top-tier." So, if you're cringing at the UI, just know that I get it. It was really about getting the functionality down, not winning any design awards.

6. **HTTP vs. HTTPS**: Because I skipped setting up HTTPS, you’re going to get one of those scary browser warnings saying the connection is insecure. You'll have to confirm that you really, really want to proceed with HTTP. This is mostly an issue with the WebSocket (`wss`), since AWS makes it a bit tricky (or costly) to proxy WebSocket servers with HTTPS. The REST endpoints themselves could easily be secured over HTTPS, so at least there's that.

7. **Copy-to-Clipboard Issue**: The lack of HTTPS also means that the "copy-to-clipboard" feature in the UI doesn't work in production, since modern browsers are pretty strict about clipboard permissions for insecure pages. It’s funny, because a workaround that worked great seven years ago just doesn't fly today. So, if you want to share a chat link, you'll have to go old-school: select it manually, right-click, and hit copy. Sometimes the classics are all we’ve got!
