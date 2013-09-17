package org.scaloid.layout.web

import spray.http._
import spray.http.MediaTypes._



trait Views {

  def index(original: Option[String] = None, converted: Option[String] = None) = renderHtmlWithDocType(indexHtml(original, converted))

  private def indexHtml(original: Option[String], converted: Option[String]) =
<html>
  <head>
      <meta charset="utf-8"/>
      <title>Scaloid layout converter</title>
      <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
      <meta name="description" content=""/>
      <meta name="author" content=""/>

      <!-- Le styles -->
      <link href="//netdna.bootstrapcdn.com/bootstrap/3.0.0/css/bootstrap.min.css" rel="stylesheet"/>

      <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
      <!--[if lt IE 9]>
      <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
      <![endif]-->
  </head>
  <body>

    <div class="container">

      <div class="row">
        <h1>Scaloid Layout Converter</h1>
        <p>This program converts an Android XML layout into a <a href="http://scaloid.org">Scaloid</a> layout.</p>
      </div>

      <div class="row">
        <h4>Paste Android XML layout here: </h4>
        <form method="post" enctype="multipart/form-data">
          <textarea name="source" class="col-xs-12" rows={original.fold("20")(_ => "10")}>{original.getOrElse("")}</textarea>
          <br/>
          <input type="submit" class="btn btn-primary btn-large btn-block"/>
          <br/>
        </form>
      </div>

      {if (converted.isDefined)
        <div class="row">
          <h4>Converted <a href="http://scaloid.org">Scaloid</a> layout is:</h4>
          <pre class="prettyprint col-xs-12">{converted.get}</pre>
        </div>
      }

      <div class="row">
        <div class="panel panel-warning">
          <div class="panel-heading">Note</div>
          <div class="panel-body">
            Currently, this converter is in beta stages. The conversion result may omit some properties from the original XML.
            <br/>
            Please check the equality of the layout manually.
          </div>
        </div>

        <p><a href="https://github.com/pocorall/scaloid-layout-converter">Fork this project on Github</a> and please help improve this!</p>
      </div>

    </div>

    <!-- Le javascript
    ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script src="http://code.jquery.com/jquery-1.10.1.min.js"></script>
    <script src="//netdna.bootstrapcdn.com/bootstrap/3.0.0/js/bootstrap.min.js"></script>
    <script src="https://google-code-prettify.googlecode.com/svn/loader/run_prettify.js"></script>
  </body>
</html>
  
  import xml._
  private def renderHtmlWithDocType(html: Node)= {
    val w = new java.io.StringWriter()
    XML.write(w, html, "UTF-8", xmlDecl = false, doctype = dtd.DocType("html", dtd.SystemID(""), Nil))
    HttpEntity(`text/html`, w.toString)
  }


}
