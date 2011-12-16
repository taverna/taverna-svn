<%--
BEGIN
Andrew Vos' Cross Browser (JavaScript/DHTML) TreeView
http://www.codeproject.com/KB/scripting/IE_FF_DHTML_TreeView.aspx
--%>

<% String imagesPath = response.encodeURL(request.getContextPath() + "/taverna/images/") ; %>

<script type="text/javascript">

var parentNodeCount = 0;
var nodeCount = 0;

function onParentNodeImageClick(node) {
    var divNode = document.getElementById(node.name.replace("parentNodeImage","parentNodeDiv"));
	var imageNode = node;

 	if (divNode.style.display == "none"){
		imageNode.src = "<%= imagesPath %>minus.png";
		divNode.style.display = "";
	}
	else {
		imageNode.src = "<%= imagesPath %>plus.png";
		divNode.style.display = "none";
	}
}
function onParentNodeTextClick(node) {
    var divNode = document.getElementById(node.name.replace("parentNodeText","parentNodeDiv"));
    var imageNode = document.getElementsByName(node.name.replace("parentNodeText","parentNodeImage"))[0];

	if (divNode.style.display == 'none') {
		imageNode.src = "<%= imagesPath %>minus.png";
		divNode.style.display = '';
	}
	else {
		imageNode.src = "<%= imagesPath %>plus.png";
		divNode.style.display = 'none';
	}
}

function onNodeTextClick(node) {
    var imageNode = document.getElementsByName(node.name.replace("nodeText","nodeImage"))[0];
    setSelectedNode(imageNode);
}
function onNodeImageClick(node, url, target){
    var imageNode = node
    setSelectedNode(imageNode);
}

function setSelectedNode(imageNode){
    for (index = 0; index < this.nodeCount; index++) {
		document.getElementsByName("nodeImage" + index)[0].src = "<%= imagesPath %>page.png";
    }
   	imageNode.src = "<%= imagesPath %>pageSelected.png";
}
function expandAll(){
	for (index = 0; index < this.parentNodeCount; index++) {
		document.getElementById("parentNodeDiv" + index).style.display = "";
        document.getElementsByName("parentNodeImage" + index)[0].src = "<%= imagesPath %>minus.png";
	}
}
function collapseAll(){
	for (index = 0; index < this.parentNodeCount; index++) {
		document.getElementById("parentNodeDiv" + index).style.display = "none";
        document.getElementsByName("parentNodeImage" + index)[0].src = "<%= imagesPath %>plus.png";
	}
}

function startParentNode(table_class, text){
	document.write('<table class="'+table_class+'">');
	document.write('  <tr>');
	document.write('    <td><img src="<%= imagesPath %>plus.png" name="parentNodeImage' + parentNodeCount + '" onclick="onParentNodeImageClick(this)" style="cursor:pointer;"/></td>');
	document.write('    <td><a class="parentTreeNode" name="parentNodeText' + parentNodeCount + '" onclick="onParentNodeTextClick(this)" style="cursor:pointer;">');
	document.write(text);
	document.write('</a>');
	document.write('  </td>');
	document.write('  </tr>');
	document.write('  <tr>');
	document.write('    <td></td><!-- SPACING -->');
	document.write('	<td><DIV id="parentNodeDiv' + parentNodeCount + '" style="display:none">');
    this.parentNodeCount = this.parentNodeCount + 1;
}
function endParentNode(){
	document.write('</DIV></td>');
	document.write('  </tr>');
	document.write('</table>');
}
function addNode(table_class, text, url, target){
	document.write('<table class="'+table_class+'">');
	document.write('  <tr>');
	document.write('	<td>');
        document.write('<a href="' + url + '" target="' + target + '" onfocus="this.hideFocus=true;" style="outline-style:none;">');
	document.write('<img src="<%= imagesPath %>page.png" border="0" name="nodeImage' + this.nodeCount + '" onclick="onNodeImageClick(this);" />');
        document.write('</a>');
	document.write('	</td>');
	document.write('    <td><a name="nodeText' + this.nodeCount + '" onclick="onNodeTextClick(this);" href="' + url + '" target="' + target + '" class="normalTreeNode" onfocus="this.hideFocus=true;" style="outline-style:none;">' + text + '</a></td>');
	document.write('  </tr>');
	document.write('</table>');
        this.nodeCount = this.nodeCount + 1;
}
function addNode2(table_class, textarea_id, text, url, content_id){
	document.write('<table class="'+table_class+'">');
	document.write('  <tr>');
	document.write('	<td>');
        document.write('<a href="javascript:ajaxpage(\''+textarea_id+'\', \''+url+'\', \''+content_id+'\');" onfocus="this.hideFocus=true;" style="outline-style:none;">');
	document.write('<img src="<%= imagesPath %>page.png" border="0" name="nodeImage' + this.nodeCount + '" onclick="onNodeImageClick(this);" />');
        document.write('</a>');
	document.write('	</td>');
	document.write('    <td><a name="nodeText' + this.nodeCount + '" onclick="onNodeTextClick(this);" href="javascript:ajaxpage(\''+textarea_id+'\', \''+url+'\', \''+content_id +'\');" class="normalTreeNode" onfocus="this.hideFocus=true;" style="outline-style:none;">' + text + '</a></td>');
	document.write('  </tr>');
	document.write('</table>');
        this.nodeCount = this.nodeCount + 1;
}
function addExpandCollapseAll(table_class){
	document.write('<table class="'+table_class+'">');
	document.write('  <tr>');
	document.write('    <td align="right" width="50%"><a onclick="expandAll();" class="expandCollapse" style="cursor:pointer;">Expand All</a></td>');
	document.write('    <td alight="left" width="50%"><a onclick="collapseAll();" class="expandCollapse" style="cursor:pointer;">Collapse All</a></td>');
	document.write('  </tr>');
	document.write('</table>');
}
<%--
END
Andrew Vos' Cross Browser (JavaScript/DHTML) TreeView
http://www.codeproject.com/KB/scripting/IE_FF_DHTML_TreeView.aspx
--%>
</script>
