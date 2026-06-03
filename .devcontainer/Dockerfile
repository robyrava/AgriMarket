FROM mcr.microsoft.com/devcontainers/java:21

# Risolve problemi di chiavi GPG obsolete su immagini basate su Debian
RUN rm -f /etc/apt/sources.list.d/yarn.list \
    && apt-get update && export DEBIAN_FRONTEND=noninteractive \
    && apt-get -y install --no-install-recommends postgresql-client

# Installazione Antigravity CLI
RUN curl -fsSL https://antigravity.google/cli/install.sh | bash
