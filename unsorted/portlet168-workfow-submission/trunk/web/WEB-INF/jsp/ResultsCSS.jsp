
<%--
CSS style for the workflow results table
Based on Andrew Vos' Cross Browser (JavaScript/DHTML) TreeView CSS style
http://www.codeproject.com/KB/scripting/IE_FF_DHTML_TreeView.aspx
--%>

<style type="text/css">

<%-- jobs table style --%>
table.jobs
{
    border-collapse:collapse;
    margin-bottom:10px;
    width: 100%;
}
table.jobs, table.jobs th, table.jobs td
{
    border: 1px solid #084B8A;
    padding: 5px;
}
table.jobs th {
    background-color: #CEE3F6;
    color: #084B8A;
}

<%-- results table style --%>
table.results
{
    border-collapse:collapse;
    margin-bottom:10px;
    width: 100%;
}
table.results, table.results th, table.results td
{
    border: 1px solid #4A5C4A;
    padding: 5px;
}
table.results th {
    background-color: #B8E6B8;
    color: #526652;
}

table.result_data
{
    border-collapse:collapse;
}
table.result_data, table.result_data th, table.result_data td
{
    border: none;
    padding: 0px;
}

<%-- Andrew Vos' Cross Browser (JavaScript/DHTML) TreeView CSS style --%>
.parentTreeNode {
	color: #000000;
	font-size:xx-small;
        text-decoration: none;
	border: 1px solid transparent;
	padding: 0px 3px 0px 3px;
	font-family: Verdana, Arial, Helvetica, sans-serif;
}
.parentTreeNode:hover {
	color: #666666;
	border: 1px solid #000000;
	background-color: #EEEEEE;
}
.normalTreeNode {
	color: #000000;
	font-size:xx-small;
	text-decoration: none;
	border: 1px solid transparent;
	display: block;
	padding: 0px 3px 0px 3px;
	font-family: Verdana, Arial, Helvetica, sans-serif;
}
.normalTreeNode:hover {
	color: #666666;
	border: 1px solid #000000;
	background-color: #EEEEEE;
}
.expandCollapse {
	color: #000000;
	font-size:xx-small;
	border: 1px solid transparent;
	padding: 0px 3px 0px 3px;
	font-family: Verdana, Arial, Helvetica, sans-serif;
}
.expandCollapse:hover {
	color: #666666;
	background-color: #EEEEEE;
	border: 1px solid #000000;
}

.output_name{
    font-weight: bold;
}

.output_depth{
    font-weight: normal;
    color: #000000;
}

.output_mime_type{
    font-style: italic;
    color: #666;
}

</style>
