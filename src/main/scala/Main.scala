package jb

import com.github.jknack.handlebars.{ Context, Handlebars }
import fixiegrips.{ ScalaHelpers, Json4sResolver }
import org.json4s.native.JsonMethods.{ parse, parseOpt }
import java.io.File
import io.Source

object Main {

  val handlebars = new Handlebars().registerHelpers(ScalaHelpers)

  def ctx(obj: Object) =
    Context.newBuilder(obj).resolver(Json4sResolver).build

  case class Options(
    src: Source          = Source.fromInputStream(System.in),
    tmpl: Option[Source] = None
  )

  val Flag = """--(\w+)=(.+)""".r

  def main(args: Array[String]) {
    val opts = (Options() /: args) {
      case (o, Flag("template", path)) =>
        val f = new File(path)
        if (!f.exists) sys.error(s"template $path does not exist")
        else o.copy(tmpl = Some(Source.fromFile(f)))
      case (o, Flag("templatesrc", src)) =>
        o.copy(tmpl = Some(Source.fromString(src)))
      case (o, Flag("json", json)) =>
        parseOpt(json).map(_ => o.copy(src = Source.fromString(json)))
          .getOrElse(sys.error(s"malformed json $json"))
    }
    
    val template = handlebars.compileInline(
      opts.tmpl.map(_.getLines.mkString).getOrElse("{{.}}")
    )
    val json = parse(opts.src.getLines.mkString)

    println(template(ctx(json)))
  }
}
