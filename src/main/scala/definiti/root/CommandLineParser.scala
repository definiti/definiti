package definiti.root

import java.nio.file.{Files, Path, Paths}

import definiti.root.utils.{Invalid, Valid, Validation}
import scopt.OptionParser

case class CommandLineParser(
  config: String = "./definiti.conf"
)

object CommandLineParser {
  val versionNumber = "0.0.0"

  val parser: OptionParser[CommandLineParser] = new OptionParser[CommandLineParser]("definiti") {
    head("definiti", versionNumber)

    opt[String]("config")
      .action((value, commandLineParser) => commandLineParser.copy(config = value))
      .text("Set the configuration file path (default: ./definiti.conf)")

    help("help").text("prints this usage text")
  }
}

case class CommandLineConfiguration(
  config: Path
)

object CommandLineConfiguration {
  def apply(commandLineParser: CommandLineParser): Validation[CommandLineConfiguration] = {
    validate(commandLineParser)
  }

  private def validate(commandLineParser: CommandLineParser): Validation[CommandLineConfiguration] = {
    validateConfigPath(commandLineParser.config) match {
      case Valid(config) => Valid(CommandLineConfiguration(config))
      case Invalid(errors) => Invalid(errors)
    }
  }

  def validateConfigPath(config: String): Validation[Path] = {
    val configPath = Paths.get(config)
    if (Files.exists(configPath)) {
      Valid(configPath)
    } else {
      Invalid(s"The configuration file ${config} does not exist.")
    }
  }
}