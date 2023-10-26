
## How to run the application as a Podman container

### Build the container with podman

```bash
cd ~/.local/src/TLC
podman build -t computateorg/orionld-smartvillage-sync:latest .
```

### Push the container up to quay.io
```bash
podman login quay.io
podman push computateorg/orionld-smartvillage-sync:latest quay.io/computateorg/orionld-smartvillage-sync:latest
```

### Run the container for local development

```bash
podman run computateorg/orionld-smartvillage-sync:latest
```
