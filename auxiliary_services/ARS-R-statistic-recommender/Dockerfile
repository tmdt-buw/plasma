FROM python:3.9

WORKDIR /app
COPY requirements.txt requirements.txt

RUN pip3 install -r requirements.txt

COPY requirements.txt .
COPY linkprediction linkprediction
COPY modelextension modelextension
COPY linkedmodelextension linkedmodelextension
COPY modelbuilder modelbuilder
COPY util util
COPY *.py .

CMD [ "python3", "service.py"]