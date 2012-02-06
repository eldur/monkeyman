/*
 * Monkeyman static web site generator
 * Copyright (C) 2012  Wilfred Springer
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package nl.flotsam.monkeyman

import decorator.haml.ScalateToHtmlDecorator
import decorator.markdown.MarkdownToHtmlDecorator
import decorator.permalink.PermalinkDecorator
import decorator.registry.RegistryDecorator
import decorator.yaml.YamlFrontmatterDecorator
import java.io.File
import org.apache.commons.io.FilenameUtils._
import org.fusesource.scalate.{Binding, Template, TemplateEngine}

class MonkeymanConfiguration(sourceDir: File, layoutDir: File) {

  private val layoutFileName = "layout"
  
  private val templateEngine =
    new TemplateEngine(List(layoutDir, sourceDir))

  private val fileSystemResourceLoader = 
    new FileSystemResourceLoader(sourceDir)

  private val layoutResolver = new LayoutResolver {
    def resolve(path: String) =
      tryLoadTemplate(new File(layoutDir, getPath(path)))
  }

  val registryDecorator = new RegistryDecorator
  templateEngine.bindings = new Binding(
    name = "allResources",
    className = "Seq[nl.flotsam.monkeyman.Resource]",
    defaultValue = Some("Seq.empty[nl.flotsam.monkeyman.Resource]")
  ) :: templateEngine.bindings

  
  val resourceLoader = new DecoratingResourceLoader(fileSystemResourceLoader,
    new YamlFrontmatterDecorator(),
    new MarkdownToHtmlDecorator(templateEngine, layoutResolver, registryDecorator.allResources _),
    new ScalateToHtmlDecorator(templateEngine, registryDecorator.allResources _),
    PermalinkDecorator,
    registryDecorator
  )
  
  private def tryLoadTemplate(dir: File): Option[Template] = {
    val files = 
      TemplateEngine.templateTypes.view.map(ext => new File(dir, layoutFileName + "." + ext))
    files.find(_.exists()) match {
      case Some(file) =>
        Some(templateEngine.load(file))
      case None =>
        if (dir != layoutDir) tryLoadTemplate(dir.getParentFile)
        else None
    }
    
  }

}