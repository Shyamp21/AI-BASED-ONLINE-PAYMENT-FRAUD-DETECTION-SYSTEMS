FROM python:3.11-slim

WORKDIR /app

# Install system build dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    curl \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY . .

# Generate PaySim sample dataset and train models if not already generated
RUN python train_models.py

EXPOSE 5001

ENV ML_PORT=5001
ENV PYTHONUNBUFFERED=1

CMD ["python", "app.py"]
