# Google Cloud Deployment Guide for Backend

## 1. Prerequisites
- Google Cloud SDK installed and authenticated
- Cloud SQL instance created (MySQL)
- Update `src/main/resources/application-cloudsql.properties` with your DB details

## 2. Build the Spring Boot JAR
```
./mvnw clean package
```

## 3. Deploy to App Engine
```
gcloud app deploy
```

## 4. Set environment variables if needed (e.g., DB credentials)

---

# Google Cloud SQL Setup
- Create a Cloud SQL instance (MySQL) in Google Cloud Console
- Note the instance connection name, DB name, user, and password
- Update your `application-cloudsql.properties` accordingly

---

# Switch Spring profile to use Cloud SQL config
- Set `SPRING_PROFILES_ACTIVE=cloudsql` in your App Engine environment variables
