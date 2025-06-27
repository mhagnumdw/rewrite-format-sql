#!/bin/bash

# // TODO: deletar esse arquivo após ter configurado o processo de release completamente

./mvnw -V -B release:clean release:prepare -DreleaseVersion=1.0.0

./mvnw -V release:perform
