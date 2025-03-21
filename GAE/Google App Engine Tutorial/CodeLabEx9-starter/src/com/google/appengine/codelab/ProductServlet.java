/**
 * Copyright 2011 Google
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.appengine.codelab;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.codelab.Util;

/**
 * This servlet responds to the request corresponding to products. The servlet
 * manages the Product Entity
 * 
 * @author
 */
@SuppressWarnings("serial")
public class ProductServlet extends BaseServlet {

  private static final Logger logger = Logger.getLogger(ProductServlet.class.getCanonicalName());

  /**
   * Get the entities in JSON format.
  */
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			      throws ServletException, IOException {
    super.doGet(req, resp);
    logger.log(Level.INFO, "Obtaining product listing");
    String searchFor = req.getParameter("q");
    PrintWriter out = resp.getWriter();
    Iterable<Entity> entities = null;
    if (searchFor == null || searchFor.equals("") || searchFor == "*") {
      entities = Product.getAllProducts("Product");
	      out.println(Util.writeJSON(entities));
    } else {
	      Entity e = Product.getProduct(searchFor);
	      if (e != null) {
		        Set<Entity> result = new HashSet<Entity>();
		        result.add(e);
		        out.println(Util.writeJSON(result));
	      }
    }
  }

	/**
	 * Create the entity and persist it.
	 */
  protected void doPut(HttpServletRequest req, HttpServletResponse resp)
	      throws ServletException, IOException {
	    logger.log(Level.INFO, "Creating Product");
    PrintWriter out = resp.getWriter();
    String category = req.getParameter("name");
    String description = req.getParameter("description");
    try {
      Product.createOrUpdateProduct(category, description);
    } catch (Exception e) {
      String msg = Util.getErrorResponse(e);
      out.print(msg);
    }
  }

	/**
	 * Delete the product. Gives an error when we try to delete the product that has items
	 * associated with it
	 */
  protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
	      throws ServletException, IOException {
    logger.log(Level.INFO, "Deleting the product");
    String productkey = req.getParameter("id");
    Key key = KeyFactory.createKey("Product", productkey);
    PrintWriter out = resp.getWriter();
    Iterable<Entity> entities = Util.listEntities("Item", "product", productkey);
    for (Entity e : entities) {
      if (e != null)
		        out.println("Cannot delete product as there are items associated with this product.");
	      return;
    }
	    try {
	      Util.deleteFromCache(key);
	      Util.deleteEntity(key);
	    } catch (Exception e) {
	      String msg = Util.getErrorResponse(e);
	      out.print(msg);
    }
  }

	/**
	 * Redirect the call to doDelete or doPut method
	 */
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			      throws ServletException, IOException {
    String action = req.getParameter("action");
    if (action.equalsIgnoreCase("delete")) {
      doDelete(req, resp);
	      return;
    } else if (action.equalsIgnoreCase("put")) {
	      doPut(req, resp);
      return;
    }
  }
}