#!/bin/bash

set -e

# Highlight
CODE='\033[0;97m'
STRONG='\033[0;94m'
END='\033[0m'

main() {
  local version
  local languages
  local plugins

  introduction

  askCoreVersion version
  askLanguages languages
  askPlugins plugins

  writeScriptFile ${version}
  writeConfFile ${version} ${languages} ${plugins}

  echo -e "\n"
  echo -e "${STRONG}Installation completed!${END}"

  howToInstallNut
  howToContinue
}

introduction() {
  echo -e "Installation of ${STRONG}Definiti${END}"
  echo ""
  echo -e "You will be asked some questions before executing the installation"
  echo -e "You can stop it anytime by typing ${CODE}^C${END}"
  echo ""
}

askCoreVersion() {
  echo -e "${STRONG}Which version do you want to use?${END}"
  echo -e "Available versions:"
  echo -e "- ${CODE}0.3.0-SNAPSHOT${END} (beta)"
  while true; do
    local _answer=""
    echo -e "\nSelect version (default: ${CODE}0.3.0-SNAPSHOT${END}): "
    read _answer
    case ${_answer} in
      "0.3.0-SNAPSHOT" )
        eval $1="0.3.0-SNAPSHOT"
        break;;
      "" )
        eval $1="0.3.0-SNAPSHOT"
        break;;
      * ) echo "Unrecognized version";;
    esac
  done
}

askLanguages() {
  echo -e "${STRONG}Which languages do you want to use?${END}"
  echo -e "Available languages:"
  echo -e "- ${CODE}scala${END}"
  echo -e "- ${CODE}typescript${END}"
  local _result=""
  while true; do
    local _answer=""
    echo -e "\nSelect language (empty if you want to stop): "
    read _answer
    case ${_answer} in
      "scala" )
        _result="${_result};scala;";
        ;;
      "typescript" )
        _result="${_result};ts;";
        ;;
      "ts" )
        _result="${_result};ts;";
        ;;
      "" )
        break;;
      * ) echo "Unrecognized language";;
    esac
  done
  eval $1="'${_result}'"
}

askPlugins() {
  echo -e "${STRONG}Which plugins do you want to use?${END}"
  echo -e "Available plugins:"
  echo -e "- ${CODE}tests${END}"
  echo -e "- ${CODE}glossary${END}"
  local _result=""
  while true; do
    local _answer=""
    echo -e "\nSelect plugin (empty if you want to stop): "
    read _answer
    case ${_answer} in
      "test" )
        _result="${_result};tests;";
        ;;
      "tests" )
        _result="${_result};tests;";
        ;;
      "glossary" )
        _result="${_result};glossary;";
        ;;
      "" )
        break;;
      * ) echo "Unrecognized plugin";;
    esac
  done
  eval $1="'${_result}'"
}

# writeScriptFile version
writeScriptFile() {
  local version=$1

  cat > definiti <<EOT
#!/bin/bash

set -e

docker run --rm --interactive -v \${PWD}:/definiti -w /definiti definiti/definiti:${version} definiti
EOT
  chmod +x definiti
}

# writeConfFile version languages plugins
writeConfFile() {
  local version=$1
  local languages=$2
  local plugins=$3

  cat > definiti.conf <<EOT
definiti {
  dependencies = [
    $(createDependencies ${version} ${languages} ${plugins})
  ]

  api {
    version = "${version}"
  }

  core {
    source = "src/main/definiti"

    generators = [
      $(createGenerators ${languages} ${plugins})
    ]​

    contexts = [
      $(createContexts ${plugins})
    ]

    parsers = []
    validators = []
    flags = {}
  }
}
EOT
}

# createDependencies version languages plugins
createDependencies() {
  local version=$1
  local languages=$2
  local plugins=$3

  echo "\"io.github.definiti:core_2.12:${version}\","

  if [[ ${languages} = *";scala;"* ]]; then
    echo "    \"io.github.definiti:scala-model_2.12:${version}\","
  fi

  if [[ ${languages} = *";ts;"* ]]; then
    echo "    \"io.github.definiti:ts-model_2.12:${version}\","
  fi

  if [[ ${plugins} = *";tests;"* ]]; then
    echo "    \"io.github.definiti:tests_2.12:${version}\","

    if [[ ${languages} = *";scala;"* ]]; then
      echo "    \"io.github.definiti:scala-tests_2.12:${version}\","
    fi
  fi

  if [[ ${plugins} = *";glossary;"* ]]; then
    echo "    \"io.github.definiti:glossary_2.12:${version}\","
  fi
}

# createGenerators languages plugins
createGenerators() {
  local languages=$1
  local plugins=$2

  if [[ ${languages} = *";scala;"* ]]; then
    echo "      \"definiti.scalamodel.plugin.ScalaModelGeneratorPlugin\","
  fi

  if [[ ${languages} = *";ts;"* ]]; then
    echo "      \"definiti.tsmodel.plugin.TsModelGeneratorPlugin\","
  fi

  if [[ ${plugins} = *";tests;"* ]]; then
    if [[ ${languages} = *";scala;"* ]]; then
      echo "      \"definiti.scalatests.ScalaTestsGeneratorPlugin\","
    fi
  fi

  if [[ ${plugins} = *";glossary;"* ]]; then
    echo "      \"definiti.glossary.plugin.GlossaryGeneratorPlugin\","
  fi
}

# createContexts plugins
createContexts() {
  local plugins=$1

  if [[ ${plugins} = *";tests;"* ]]; then
    echo "\"definiti.tests.TestsPlugin\""
  fi
}

howToInstallNut() {
  echo -e ""
  echo -e "${STRONG}====================${END}"
  echo -e "${STRONG}=${END} You also need to install ${CODE}nut${END} to use ${CODE}Definiti${END}"
  echo -e "${STRONG}=${END} If you have npm, you can execute following command:"
  echo -e "${STRONG}=${END} ${CODE}npm install -g nut-bin${END}"
  echo -e "${STRONG}=${END} In other cases, please visit: https://github.com/matthieudelaro/nut"
  echo -e "${STRONG}====================${END}"
  echo -e ""
}

howToContinue() {
  echo -e ""
  echo -e "How to use Definiti:"
  echo -e "- Write your code inside ${CODE}src/main/definiti${END}"
  echo -e "- Execute: ${CODE}nut run${END}"
  echo -e "- See generated files in ${CODE}target${END} folder"
  echo -e ""
  echo -e "Example of definiti file:"
  echo -e "${CODE}"
  cat <<EOT
// file blog.def
package my.blog

type BlogArticle {
  title: String
  description: String
  date: Date
}
EOT
echo -e "${END}"
}

main