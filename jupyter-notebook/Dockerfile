FROM python:3.9.15-slim-bullseye
RUN apt update && apt install openjdk-17-jdk wget unzip -y
RUN pip install notebook
RUN wget https://github.com/SpencerPark/IJava/releases/download/v1.3.0/ijava-1.3.0.zip -O /tmp/ijava-1.3.0.zip
RUN cd /tmp && unzip ijava-1.3.0.zip && python3 install.py
EXPOSE 8888
CMD jupyter notebook /opt/notebooks --ip 0.0.0.0 --allow-root --NotebookApp.token='' --NotebookApp.password=''
