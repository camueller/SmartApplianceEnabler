# AWS Setup

- EC2 Instance anlegen:
  - Amazon Linux 2 AMI (HVM), SSD Volume Type
  - Type: t2.micro
- ggf. Keypair erzeugen
- Security Group anlegen mit Inbound Access für SSH + HTTP (Source: Anywhere)
- Security Group zuweisen: Actions -> Networking -> Change Security Groups

In der gestarteten Instanz folgende Befehle ausführen:
```console
sudo yum update -y
sudo yum install docker -y
sudo service docker start
sudo service docker status
```

## Hinweis
Falls das _EC2 Dashboard_ laufende Instanzen nicht anzeigt, stimmt möglicherweise die Regio nicht mit der Region überein, in der die Instanzen angelegt wurden (z.B. us-east-2).