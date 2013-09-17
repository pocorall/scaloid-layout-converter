package org.scaloid.layout.web

import spray.http._
import spray.http.MediaTypes._


trait Views {

  def index(original: String, converted: String = "") = renderHtmlWithDocType(indexHtml(original, converted))

  private def indexHtml(original: String, converted: String) =
<html>
  <head>
      <meta charset="utf-8"/>
      <title>Scaloid layout converter</title>
      <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
      <meta name="description" content=""/>
      <meta name="author" content=""/>

      <!-- Le styles -->
      <link href="http://twitter.github.com/bootstrap/assets/css/bootstrap.css" rel="stylesheet"/>
      <link href="http://twitter.github.com/bootstrap/assets/css/bootstrap-responsive.css" rel="stylesheet"/>
      <link href="http://twitter.github.com/bootstrap/assets/css/docs.css" rel="stylesheet"/>
      <link href="http://twitter.github.com/bootstrap/assets/js/google-code-prettify/prettify.css" rel="stylesheet"/>

      <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
      <!--[if lt IE 9]>
      <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
      <![endif]-->
  </head>
  <body>
    <h1>Scaloid Layout Converter</h1>

    <p>This program converts an Android XML layout into a <a href="http://scaloid.org">Scaloid</a> layout.</p>


    <h4>Paste Android XML layout here: </h4>

    <form method="post" enctype="multipart/form-data">
        <textarea name="source" class="span10" rows="10">{original}</textarea> <br/>

        <input type="submit" class="btn btn-primary"/>
    </form>

    Converted <a href="http://scaloid.org">Scaloid</a> layout is:<br/>

    <textarea class="span10" rows="10">{converted}</textarea> <br/>

    <p><span class="label label-important">Currently, this converter is in beta stages. The conversion result may omit some properties from the original XML.<br/>Please check the equality of the layout manually.</span>
    </p>

    <p><a href="https://github.com/pocorall/scaloid-layout-converter">Fork this
        project on Github</a> and please help improve this!</p>


    <!-- Le javascript
    ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script type="text/javascript" src="http://platform.twitter.com/widgets.js"></script>
    <script src="http://twitter.github.com/bootstrap/assets/js/jquery.js"></script>
    <script src="http://twitter.github.com/bootstrap/assets/js/bootstrap-transition.js"></script>
    <script src="http://twitter.github.com/bootstrap/assets/js/bootstrap-alert.js"></script>
    <script src="http://twitter.github.com/bootstrap/assets/js/bootstrap-modal.js"></script>
    <script src="http://twitter.github.com/bootstrap/assets/js/bootstrap-dropdown.js"></script>
    <script src="http://twitter.github.com/bootstrap/assets/js/bootstrap-scrollspy.js"></script>
    <script src="http://twitter.github.com/bootstrap/assets/js/bootstrap-tab.js"></script>
    <script src="http://twitter.github.com/bootstrap/assets/js/bootstrap-tooltip.js"></script>
    <script src="http://twitter.github.com/bootstrap/assets/js/bootstrap-popover.js"></script>
    <script src="http://twitter.github.com/bootstrap/assets/js/bootstrap-button.js"></script>
    <script src="http://twitter.github.com/bootstrap/assets/js/bootstrap-collapse.js"></script>
    <script src="http://twitter.github.com/bootstrap/assets/js/bootstrap-carousel.js"></script>
    <script src="http://twitter.github.com/bootstrap/assets/js/bootstrap-typeahead.js"></script>
    <script src="http://twitter.github.com/bootstrap/assets/js/bootstrap-affix.js"></script>

    <script src="http://twitter.github.com/bootstrap/assets/js/holder/holder.js"></script>
    <script src="http://twitter.github.com/bootstrap/assets/js/google-code-prettify/prettify.js"></script>

    <script src="http://twitter.github.com/bootstrap/assets/js/application.js"></script>
  </body>
</html>
  
  import xml._
  private def renderHtmlWithDocType(html: Node)= {
    val w = new java.io.StringWriter()
    XML.write(w, html, "UTF-8", xmlDecl = false, doctype = dtd.DocType("html", dtd.SystemID(""), Nil))
    HttpEntity(`text/html`, w.toString)
  }


}
