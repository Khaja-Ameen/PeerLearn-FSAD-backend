# AWS Deployment Guide for Backend

## 1. Prerequisites
- AWS CLI installed and configured
- Elastic Beanstalk CLI (EB CLI) installed
- Amazon RDS MySQL instance created

## 2. Build the Spring Boot JAR
```
./mvnw clean package
```

## 3. Deploy to Elastic Beanstalk
- Initialize EB CLI (first time only):
  ```
  eb init
  ```
- Create an environment (first time only):
  ```
  eb create <env-name>
  ```
- Deploy:
  ```
  eb deploy
  ```

## 4. Set environment variables (RDS connection, etc.)
- In AWS Console or EB CLI, set:
  - SPRING_DATASOURCE_URL
  - SPRING_DATASOURCE_USERNAME
  - SPRING_DATASOURCE_PASSWORD

---

# Amazon RDS Setup
- Create an RDS MySQL instance in AWS Console
- Note the endpoint, DB name, user, and password
- Update your `application.properties` accordingly

---

# CORS
- Ensure CORS is enabled in your Spring Boot backend for your S3/CloudFront domain
