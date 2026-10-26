# Cosmetic store 

## Steps for execute in Linux 
- Copy the `.env.docker.example` file to an `.env.example` file.
- Copy the `application.example.yml` file to an `application.yml` file int `authorization/src/main/resources` route.
- Fill the `.env.example` file with the database's credentials.
  - It's recommended that the root password variable be different from that of the read user.
- Create the RSA keys (PKCS#8 PEM format) for JWT encryption/signing:
  - Generate the private key in PKCS#8 format:
    ```bash
    openssl genpkey -algorithm RSA -out private_key.pem -pkeyopt rsa_keygen_bits:2048
    ```
  - Extract the public key in X.509 format:
    ```bash
    openssl rsa -pubout -in private_key.pem -out public_key.pem
    ```
  - Copy both files (`private_key.pem` and `public_key.pem`) into `cosmetic/src/main/resources/keys/`.
- Execute `docker compose pull` and later `docker compose up -d` commands.
- Run the `initDataBase.sh` file with `./initDataBase.sh` command.
- Run the `InitUserDb.sh` file with `./InitUserDb.sh` command.
- Replace the username and password fields in the `application.yml` file for `POSTGRES_READ_USER` and `POSTGRES_READ_PASSWORD` env values.
- Run the `executeApp.sh` file with `./executeApp.sh` command.
- Generate a crypted password with the endpoint `generate-password` in the AuthController.
  - Use this password in the `data.sql` file. Also add your own credentials for the first User for the App.
- Run the `insertData.sh` file with `./insertData.sh` command.

## Steps for execute in Windows 
- Copy the `.env.docker.example` file to an `.env.example` file.
- Copy the `application.example.yml` file to an `application.yml` file int `authorization/src/main/resources` route.
- Fill the `.env.example` file with the database's credentials.
  - It's recommended that the root password variable be different from that of the read user.
- Execute `docker compose pull` and later `docker compose up -d` commands.
- Create the RSA keys (PKCS#8 PEM format) for JWT encryption/signing:
  - Generate the private key in PKCS#8 format:
    ```cmd
    openssl genpkey -algorithm RSA -out private_key.pem -pkeyopt rsa_keygen_bits:2048
    ```
  - Extract the public key in X.509 format:
    ```cmd
    openssl rsa -pubout -in private_key.pem -out public_key.pem
    ```
  - Copy both files (`private_key.pem` and `public_key.pem`) into `cosmetic/src/main/resources/keys/`.
- Run the `initDataBase.bat` file.
- Run the `InitUserDb.bat` file.
- Replace the username and password fields in the `application.yml` file for `POSTGRES_READ_USER` and `POSTGRES_READ_PASSWORD` env values.
- Run the `executeApp.bat` file.
- Generate a crypted password with the endpoint `generate-password` in the AuthController.
  - Use this password in the `data.sql` file. Also add your own credentials for the first User for the App.
- Run the `insertData.bat` file.

### About Application 
- JDK 25
- PostgreSQL 18.6
- SpringBoot 4.1.1

### Useful commands 
- `docker compose down -v --rmi all --remove-orphans` for completly drop of the containers
