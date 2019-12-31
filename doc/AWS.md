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
sudo docker volume create sae
sudo docker pull avanux/smartapplianceenabler-amd64
docker image ls
sudo docker image ls
sudo docker run -d --rm -v sae:/opt/sae/data -p80:8080 --name=sae avanux/smartapplianceenabler-amd64
```

# Hinweis
Falls das _EC2 Dashboard_ laufende Instanzen nicht anzeigt, stimmt möglicherweise die Regio nicht mit der Region überein, in der die Instanzen angelegt wurden (z.B. us-east-2).