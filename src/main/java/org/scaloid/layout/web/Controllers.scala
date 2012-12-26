package org.scaloid.layout.web

import org.springframework.web.bind.annotation.{RequestParam, RequestMapping}
import org.springframework.ui.ModelMap
import org.springframework.stereotype.Controller
import org.springframework.util.StringUtils
import org.scaloid.layout.converter.Converter

@Controller
class Controllers {
  @RequestMapping(Array("/index.do")) def index(@RequestParam(required = false) source: String) = {
    val mm = new ModelMap()
    if (StringUtils.hasText(source)) {
      mm.addAttribute("original", source)
      mm.addAttribute("converted", new Converter(source).toString)
    }
  }
}