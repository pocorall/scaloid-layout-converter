<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE HTML>
<html>
<head>
    <meta charset="utf-8">
    <title>Getting · Bootstrap</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="author" content="">

    <!-- Le styles -->
    <link href="http://twitter.github.com/bootstrap/assets/css/bootstrap.css" rel="stylesheet">
    <link href="http://twitter.github.com/bootstrap/assets/css/bootstrap-responsive.css" rel="stylesheet">
    <link href="http://twitter.github.com/bootstrap/assets/css/docs.css" rel="stylesheet">
    <link href="http://twitter.github.com/bootstrap/assets/js/google-code-prettify/prettify.css" rel="stylesheet">

    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
    <script src="http://html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

    <!-- Le fav and touch icons -->
    <link rel="apple-touch-icon-precomposed" sizes="144x144"
          href="http://twitter.github.com/bootstrap/assets/ico/apple-touch-icon-144-precomposed.png">
    <link rel="apple-touch-icon-precomposed" sizes="114x114"
          href="http://twitter.github.com/bootstrap/assets/ico/apple-touch-icon-114-precomposed.png">
    <link rel="apple-touch-icon-precomposed" sizes="72x72"
          href="http://twitter.github.com/bootstrap/assets/ico/apple-touch-icon-72-precomposed.png">
    <link rel="apple-touch-icon-precomposed"
          href="http://twitter.github.com/bootstrap/assets/ico/apple-touch-icon-57-precomposed.png">
    <link rel="shortcut icon" href="http://twitter.github.com/bootstrap/assets/ico/favicon.png">
</head>
<body>
<h1>Scaloid Layout Converter</h1>

<p>This program converts Android XML layout into Scaloid layout.</p>


<h4>Paste Android XML layout here: </h4>

<form action="index.do" method="post">
    <textarea name="source" class="span10" rows="10">${original}</textarea> <br/>

    <input type="submit" class="btn btn-primary"/>
</form>


Converted Scaloid layout is:<br/>

<textarea class="span10" rows="10">${converted}</textarea> <br/>
<span class="label label-important"><i icon="icon-warning"></i> Currently, this converter is in Alpha stages. The conversion result may omit some properties from original XML layout. Please check the equality of the layout manually.</span>


<p>This project is currently in alpha stage. <a href="https://github.com/pocorall/scaloid-layout-converter">Fork this
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
