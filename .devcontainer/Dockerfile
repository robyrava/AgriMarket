FROM mcr.microsoft.com/devcontainers/java:21

# Risolve problemi di chiavi GPG obsolete su immagini basate su Debian
RUN rm -f /etc/apt/sources.list.d/yarn.list \
    && apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get -y install --no-install-recommends postgresql-client

# Docker CLI + Compose (per connettersi al socket Docker dell'host)
RUN install -m 0755 -d /etc/apt/keyrings \
    && curl -fsSL https://download.docker.com/linux/debian/gpg -o /etc/apt/keyrings/docker.asc \
    && chmod a+r /etc/apt/keyrings/docker.asc \
    && echo "deb [arch=amd64 signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/debian bookworm stable" > /etc/apt/sources.list.d/docker.list \
    && apt-get update && apt-get -y install --no-install-recommends \
        docker-ce-cli docker-compose-plugin \
    && groupadd -f docker \
    && usermod -aG docker vscode

# Installazione Antigravity CLI
RUN curl -fsSL https://antigravity.google/cli/install.sh | bash
