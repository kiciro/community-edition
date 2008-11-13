/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.framework.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.web.framework.AbstractModelObject;
import org.alfresco.web.framework.ModelObject;
import org.alfresco.web.framework.ModelPersisterInfo;
import org.alfresco.web.scripts.Description.RequiredAuthentication;
import org.alfresco.web.site.FrameworkHelper;
import org.alfresco.web.site.RequestContext;
import org.dom4j.Document;
import org.dom4j.Element;

/**
 * Page model object
 * 
 * @author muzquiano
 */
public class Page extends AbstractModelObject
{
    public static String TYPE_ID = "page";
    public static String PROP_TEMPLATE_INSTANCE = "template-instance";
    public static String ATTR_FORMAT_ID = "format-id";
    public static String PROP_PAGE_TYPE_ID = "page-type-id";
    public static String PROP_AUTHENTICATION = "authentication";
    public static String DEFAULT_PAGE_TYPE_ID = "generic";
    
    /**
     * Instantiates a new page for a given XML document
     * 
     * @param document the document
     */
    public Page(String id, ModelPersisterInfo key, Document document)
    {
        super(id, key, document);
        
        // default page type
        if (getPageTypeId() == null)
        {
            setPageTypeId(DEFAULT_PAGE_TYPE_ID);
        }
    }

    /**
     * Gets the template id.
     * 
     * @return the template id
     */
    public String getTemplateId()
    {
        return getTemplateId(null);
    }

    /**
     * Gets the template id.
     * 
     * @param formatId the format id
     * 
     * @return the template id
     */
    public String getTemplateId(String formatId)
    {
        Element templateElement = getTemplateElement(formatId);
        if (templateElement != null)
        {
            return templateElement.getStringValue();
        }
        return null;
    }

    /**
     * Gets the template element.
     * 
     * @param formatId the format id
     * 
     * @return the template element
     */
    protected Element getTemplateElement(String formatId)
    {
        if (formatId != null && formatId.equals(FrameworkHelper.getConfig().getDefaultFormatId()))
        {
            formatId = null;
        }
        
        Element result = null;
        
        List<Element> templateElements = getDocument().getRootElement().elements(PROP_TEMPLATE_INSTANCE);
        for (int i = 0; i < templateElements.size(); i++)
        {
            Element templateElement = templateElements.get(i);
            String _formatId = templateElement.attributeValue(ATTR_FORMAT_ID);
            if (formatId == null)
            {
                if (_formatId == null || _formatId.length() == 0)
                {
                    result = templateElement;
                    break;
                }
            }
            else if (formatId.equals(_formatId))
            {
                result = templateElement;
                break;
            }
        }
        return result;
    }

    /**
     * Sets the template id.
     * 
     * @param templateId the new template id
     */
    public void setTemplateId(String templateId)
    {
        setTemplateId(templateId, null);
    }

    /**
     * Sets the template id.
     * 
     * @param templateId the template id
     * @param formatId the format id
     */
    public void setTemplateId(String templateId, String formatId)
    {
        if (formatId != null && formatId.equals(FrameworkHelper.getConfig().getDefaultFormatId()))
        {
            formatId = null;
        }
        
        Element templateElement = getTemplateElement(formatId);
        if (templateElement == null)
        {
            templateElement = getDocument().getRootElement().addElement(
                    PROP_TEMPLATE_INSTANCE);
            if (formatId != null)
                templateElement.addAttribute(ATTR_FORMAT_ID, formatId);
        }
        templateElement.setText(templateId);
    }

    /**
     * Removes the template id.
     * 
     * @param formatId the format id
     */
    public void removeTemplateId(String formatId)
    {
        if (formatId != null && formatId.equals(FrameworkHelper.getConfig().getDefaultFormatId()))
        {
            formatId = null;
        }
        
        Element templateElement = this.getTemplateElement(formatId);
        if (templateElement != null)
            templateElement.getParent().remove(templateElement);
    }

    /**
     * Gets the templates.
     * 
     * @param context the context
     * 
     * @return the templates
     */
    public Map<String, TemplateInstance> getTemplates(RequestContext context)
    {
        Map map = new HashMap(8, 1.0f);

        List templateElements = getDocument().getRootElement().elements(
                PROP_TEMPLATE_INSTANCE);
        for (int i = 0; i < templateElements.size(); i++)
        {
            Element templateElement = (Element) templateElements.get(i);
            String formatId = templateElement.attributeValue(ATTR_FORMAT_ID);
            if (formatId == null || formatId.length() == 0)
            {
                formatId = context.getConfig().getDefaultFormatId();
            }

            String templateId = templateElement.getStringValue();
            if (templateId != null)
            {
                TemplateInstance template = (TemplateInstance) context.getModel().getTemplate(templateId);
                map.put(formatId, template);
            }
        }

        return map;
    }

    /**
     * Gets the template.
     * 
     * @param context the context
     * 
     * @return the template
     */
    public TemplateInstance getTemplate(RequestContext context)
    {
        return getTemplate(context, null);
    }

    /**
     * Gets the template.
     * 
     * @param context the context
     * @param formatId the format id
     * 
     * @return the template
     */
    public TemplateInstance getTemplate(RequestContext context, String formatId)
    {
        TemplateInstance instance = null;
        String templateId = getTemplateId(formatId);
        if (templateId != null)
        {
            instance = context.getModel().getTemplate(templateId);
        }
        return instance;
    }

    /**
     * Gets the child pages.
     * 
     * @param context the context
     * 
     * @return the child pages
     */
    public Page[] getChildPages(RequestContext context)
    {
        Map<String, ModelObject> objects = context.getModel().findPageAssociations(
                this.getId(), null, PageAssociation.CHILD_ASSOCIATION_TYPE_ID);
        
        Page[] pages = new Page[objects.size()];
        
        int i = 0;
        Iterator it = objects.values().iterator();
        while(it.hasNext())
        {
            PageAssociation pageAssociation = (PageAssociation) it.next();            
            pages[i] = (Page) pageAssociation.getDestPage(context);
            i++;
        }
        
        return pages;
    }
    
    /**
     * Gets the page type id.
     * 
     * @return the page type id
     */
    public String getPageTypeId()
    {
        return this.getProperty(PROP_PAGE_TYPE_ID);        
    }
    
    /**
     * Sets the page type id.
     * 
     * @param pageTypeId the new page type id
     */
    public void setPageTypeId(String pageTypeId)
    {
        this.setProperty(PROP_PAGE_TYPE_ID, pageTypeId);        
    }
    
    /**
     * @return the Authentication required for this page
     */
    public RequiredAuthentication getAuthentication()
    {
        RequiredAuthentication authentication = RequiredAuthentication.none;
        
        String auth = this.getProperty(PROP_AUTHENTICATION);
        if (auth != null)
        {
            try
            {
               authentication = RequiredAuthentication.valueOf(auth.toLowerCase());
            }
            catch (IllegalArgumentException enumErr)
            {
               throw new AlfrescoRuntimeException(
                     "Invalid page <authentication> element value: " + auth);
            }
        }
        return authentication;
    }

    /**
     * @param authentication    the authentication level to set
     */
    public void setAuthentication(String authentication)
    {
        this.setProperty(PROP_AUTHENTICATION, authentication);
    }

    /**
     * Gets the page type.
     * 
     * @param context the context
     * 
     * @return the page type
     */
    public PageType getPageType(RequestContext context)
    {
        String pageTypeId = getPageTypeId();
        if (pageTypeId != null)
        {
            return context.getModel().getPageType(pageTypeId);
        }
        return null;
    }
       
    /* (non-Javadoc)
     * @see org.alfresco.web.site.model.AbstractModelObject#getTypeName()
     */
    public String getTypeId() 
    {
        return TYPE_ID;
    }
}
