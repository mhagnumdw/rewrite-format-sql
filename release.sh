#!/bin/bash

# set -o verbose   # O mesmo que set -v
# set -v           # Imprime as linha do script na medida que sao executadas
set -e             # Se algum comando falhar o script para (cuidado, pois nao funciona para pipes e dentro de funcoes)
# set -o pipefail  # Se algum pipe falhar o script para (complemento ao set -e)
# set -x           # Para DEBUG, mostra as linhas do código durante a execução

# // TODO: ao final traduzir para en-US como se fosse um nativo expert em desenvolvimento de software escrevendo

PS4="$(tput setaf 14)+ $(tput sgr0)"

echo

echo "Vai gerar:"
echo " - Release; e"
echo " - Próxima versão de desenvolvimento"
echo "O pom.xml é alterado, feito commit e push. Os artefatos são publicados no Maven Central Repository."

log_i() {
    echo "$(tput setaf 2)$1$(tput sgr0)"
}

log_e() {
    echo "$(tput setaf 1)[ERROR] $1$(tput sgr0)"
}

DEFAULT_BRANCH="main"
# // TODO: voltar para branch `$DEFAULT_BRANCH`
if [ "prepare-release" != "$(git branch --show-current)" ]; then
    log_e "Deve ser executado na branch $DEFAULT_BRANCH"
    exit 1
fi

if [ ! -f "./pom.xml" ]; then
    log_e "Deve ser executado na raiz do projeto"
    exit 1
fi

# // TODO: descomentar
# if [ -n "$(git fetch --dry-run)" ]; then
#     log_e "O repositório local nao esta atualizado, execute: git pull"
#     exit 1
# fi

echo

log_i "Confira os dados de usuário e email que estarão no commit"
echo -n 'user.nome : '
git config user.name
echo -n 'user.email: '
git config user.email

# // TODO: descomentar
# echo
# read -r -p "As informações estão corretas? Enter para sim, Ctrl+C para abortar"
# echo

log_i "Versão atual do pom.xml e versão do Java"
./mvnw -V help:evaluate -Dexpression=project.version -q -DforceStdout
echo
read -r -p "As informações estão corretas? Enter para sim, Ctrl+C para abortar"
echo

log_i "Gerando release..."
set -x
./mvnw clean release:clean release:prepare release:perform -Pcentral-release
set +x

echo

log_i "Mostrando os 7 últimos commits"
git log --pretty="%C(Yellow)%h  %C(reset)%ad (%C(Green)%cr%C(reset))%x09 %C(Cyan)%an (%ae): %C(reset)%s" -7

echo

log_i "Fim"
