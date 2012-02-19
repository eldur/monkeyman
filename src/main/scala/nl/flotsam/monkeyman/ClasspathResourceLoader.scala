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

import eu.medsea.mimeutil.{MimeType, MimeUtil}
import org.joda.time.LocalDateTime
import collection.JavaConversions._
import eu.medsea.mimeutil.detector.ExtensionMimeDetector


class ClasspathResourceLoader(paths: Seq[String], loader: ResourceLoader) extends ResourceLoader {
  
  private val resources = paths.map(path => ClasspathResource(path))
  
  def load = resources ++ loader.load

  def register(listener: ResourceListener) {
    loader.register(listener)
  }
  
  case class ClasspathResource(path: String) extends Resource {
    
    private val url = getClass.getResource("/" + path)
    
    def title = None

    def pubDateTime = LocalDateTime.now()

    def contentType = new ExtensionMimeDetector().getMimeTypes(url).head.toString

    def open = url.openStream()

    def tags = Set.empty

    def published = true

    def asHtmlFragment = None

    def id = path
  }
  
}
