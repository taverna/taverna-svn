/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.test;
import javax.portlet.GenericPortlet;
import javax.portlet.ActionRequest;
import javax.portlet.RenderRequest;
import javax.portlet.PortletException;
import javax.portlet.PortletSession;
import java.io.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
public class ScopeAttributes {



    public static void setWorkflowApplicationScopes(RenderRequest request)throws PortletException, IOException {

        String wfTitle;
        String wfURL;
        String wfDescription;
        String wfUploader;
        String wfTags = "";
        String wfPreview = "No workflow currently loaded";
        String wfProcs = "";
        String wfThumb;


        PortletSession session = request.getPortletSession();

        String inspectXML = null;
        //xml document of chosen workflow
        inspectXML = request.getParameter("inspect");
        String inspect = inspectXML+"&all_elements=yes";
        try{

            //build parser factory and insert xml of workflow
            DocumentBuilderFactory Factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = Factory.newDocumentBuilder();
            Document WFdoc = builder.parse(inspect);


            //creating an XPathFactory:
            XPathFactory factory = XPathFactory.newInstance();
            //using this factory to create an XPath object:
            XPath xpath = factory.newXPath();
            //XPath expressions for extracting relevant information from the workflow xml document
            XPathExpression titleExpr = xpath.compile("//title/text()");
            XPathExpression descExpr = xpath.compile("//description/text()");
            XPathExpression uploaderExpr = xpath.compile("//uploader/text()");
            XPathExpression prevExpr = xpath.compile("//preview/text()");
            XPathExpression dlExpr = xpath.compile("//content-uri/text()");
            XPathExpression tagsExpr = xpath.compile("//tags/*/text()");
            XPathExpression thumbExpr = xpath.compile("//thumbnail/text()");
            XPathExpression procsExpr = xpath.compile("//components/processors/processor/name/text()");


            //expressions are evaluated

            //title
            Object title = titleExpr.evaluate(WFdoc, XPathConstants.NODESET);
            NodeList nodeList = (NodeList) title;
            wfTitle = nodeList.item(0).getNodeValue();

            //url
            Object dl = dlExpr.evaluate(WFdoc, XPathConstants.NODESET);
            NodeList nodeList5 = (NodeList) dl;
            wfURL = nodeList5.item(0).getNodeValue();


            //description
            Object desc = descExpr.evaluate(WFdoc, XPathConstants.NODESET);
            NodeList nodeList2 = (NodeList) desc;
            wfDescription = nodeList2.item(0).getNodeValue();

            //uploader
            Object upl = uploaderExpr.evaluate(WFdoc, XPathConstants.NODESET);
            NodeList nodeList3 = (NodeList) upl;
            wfUploader = nodeList3.item(0).getNodeValue();

            //tags
            Object tags = tagsExpr.evaluate(WFdoc, XPathConstants.NODESET);
            NodeList nodeList6 = (NodeList) tags;
            for(int i=0; i<nodeList6.getLength(); i++)
            {
                wfTags += "<a>"+nodeList6.item(i).getNodeValue()+"</a> ";
            }

            //preview
            Object prev = prevExpr.evaluate(WFdoc, XPathConstants.NODESET);
            NodeList nodeList4 = (NodeList) prev;
            wfPreview = nodeList4.item(0).getNodeValue();
            
            //thumbnail
            Object thumb = thumbExpr.evaluate(WFdoc, XPathConstants.NODESET);
            NodeList thumbNodeList = (NodeList) thumb;
            wfThumb = thumbNodeList.item(0).getNodeValue();
            
            //processors
            Object procs = procsExpr.evaluate(WFdoc, XPathConstants.NODESET);
            NodeList procsNodeList = (NodeList) procs;
            for(int i=0; i<procsNodeList.getLength(); i++)
            {
                wfProcs += "<a>"+procsNodeList.item(i).getNodeValue()+"</a> ";
            }



            //Attach attributes to application scope
            session.setAttribute("title", wfTitle,PortletSession.APPLICATION_SCOPE);
            session.setAttribute("desc", wfDescription,PortletSession.APPLICATION_SCOPE);
            session.setAttribute("URL", wfURL,PortletSession.APPLICATION_SCOPE);
            session.setAttribute("upl", wfUploader,PortletSession.APPLICATION_SCOPE);
            session.setAttribute("tags", wfTags,PortletSession.APPLICATION_SCOPE);
            session.setAttribute("prev", wfPreview,PortletSession.APPLICATION_SCOPE);
            session.setAttribute("thumb", wfThumb,PortletSession.APPLICATION_SCOPE);
            session.setAttribute("procs", wfProcs,PortletSession.APPLICATION_SCOPE);
            }
        catch(Exception e){}
    }

    public static void setUserVariables(RenderRequest request)throws PortletException, IOException{

        String me = "http://www.myexperiment.org/user.xml?id=1355&elements=avatar,workflows,name,groups,friends";
        String meName;
        String meAvatar;
        String meWFNames = "";
        String meFriends = "";
        String meWFThumbs = "";
        String meWFURI = "";

        PortletSession session = request.getPortletSession();

        try{

            //build parser factory and insert xml of workflow
            DocumentBuilderFactory Factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = Factory.newDocumentBuilder();
            Document meXMLDoc = builder.parse(me);
            
            //creating an XPathFactory:
            XPathFactory factory = XPathFactory.newInstance();
            //using this factory to create an XPath object:
            XPath xpath = factory.newXPath();
            //experssions for getting user information
            XPathExpression nameExpr = xpath.compile("//name/text()");
            XPathExpression avatarExpr = xpath.compile("//avatar/@resource");
            XPathExpression friendsExpr = xpath.compile("//friends/*/text()");
            //XPathExpression workflowsExpr = xpath.compile("//workflows/*/text()");
            XPathExpression workflowsExpr = xpath.compile("//workflows/*/@uri");
            

            //name
            Object name = nameExpr.evaluate(meXMLDoc, XPathConstants.NODESET);
            NodeList nodeList7 = (NodeList) name;
            meName = nodeList7.item(0).getNodeValue();

            //avatar
            Object avatar = avatarExpr.evaluate(meXMLDoc, XPathConstants.NODESET);
            NodeList nodeList8 = (NodeList) avatar;
            meAvatar = nodeList8.item(0).getNodeValue();

            //friends
            Object friends = friendsExpr.evaluate(meXMLDoc, XPathConstants.NODESET);
            NodeList nodeList9 = (NodeList) friends;
            for(int i=0; i<nodeList9.getLength(); i++)
            {
                meFriends += nodeList9.item(i).getNodeValue()+",";
            }

            //workflows
            Object workflows = workflowsExpr.evaluate(meXMLDoc, XPathConstants.NODESET);
            NodeList nodeList10 = (NodeList) workflows;
           for(int i=0; i<nodeList10.getLength(); i++)
            {
               
                    Document wfXMLDoc = builder.parse(nodeList10.item(i).getNodeValue()+"&all_elements=yes");

                    XPathExpression xmlExpr = xpath.compile("/workflow/@uri");
                    XPathExpression titleExpr = xpath.compile("//title/text()");
                    XPathExpression descExpr = xpath.compile("//description/text()");
                    XPathExpression uploaderExpr = xpath.compile("//uploader/text()");
                    XPathExpression prevExpr = xpath.compile("//preview/text()");
                    XPathExpression dlExpr = xpath.compile("//content-uri/text()");
                    XPathExpression tagsExpr = xpath.compile("//tags/*/text()");
                    XPathExpression thumbExpr = xpath.compile("//thumbnail/text()");
                    XPathExpression procsExpr = xpath.compile("//components/processors/processor/name/text()");

                    Object title = titleExpr.evaluate(wfXMLDoc, XPathConstants.NODESET);
                    NodeList titleNodeList = (NodeList) title;
                    meWFNames += titleNodeList.item(0).getNodeValue()+",";

                    Object thumb = thumbExpr.evaluate(wfXMLDoc, XPathConstants.NODESET);
                    NodeList thumbNodeList = (NodeList) thumb;
                    meWFThumbs += thumbNodeList.item(0).getNodeValue()+ ",";

                    Object uri = xmlExpr.evaluate(wfXMLDoc, XPathConstants.NODESET);
                    NodeList uriNodeList = (NodeList) uri;
                    meWFURI += uriNodeList.item(0).getNodeValue()+ ",";
               
            }
                


            session.setAttribute("meName", meName,PortletSession.APPLICATION_SCOPE);
            session.setAttribute("meAvatar", meAvatar,PortletSession.APPLICATION_SCOPE);
            session.setAttribute("meFriends", meFriends,PortletSession.APPLICATION_SCOPE);
            session.setAttribute("meWFNames", meWFNames,PortletSession.APPLICATION_SCOPE);
            session.setAttribute("meWFThumbs", meWFThumbs,PortletSession.APPLICATION_SCOPE);
            session.setAttribute("meWFURIs", meWFURI,PortletSession.APPLICATION_SCOPE);
        }
        catch(Exception e){}
        
   }

    
}
