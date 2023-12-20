
## How to run the application as a Podman container

### Build the container with podman

```bash
cd ~/.local/src/ngsild-smartvillage-sync
podman build -t computateorg/ngsild-smartvillage-sync:latest .
```

### Push the container up to quay.io
```bash
podman login quay.io
podman push computateorg/ngsild-smartvillage-sync:latest quay.io/computateorg/ngsild-smartvillage-sync:latest
```

### Run the container for local development

```bash
podman run computateorg/ngsild-smartvillage-sync:latest
```
