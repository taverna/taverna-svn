<%--
CSS style for the myExperiment workflow search.
--%>
<style type="text/css">

div.outer {
	padding-top: 0px;
	padding-bottom: 0px;
	padding-left: 10px;
	padding-right: 10px;
	background: #FFFFFF;
}

a {
	color: #000099;
	text-decoration: none;
	overflow: auto;
}

div.list_item_container {
  padding-left: 10px;
	padding-right: 10px;
	background: #FFFFFF;
}

div.list_item {
	margin-top: 10px;
}

.list_item .title {
	text-align: left;
	font-weight: bold;
	margin-bottom: 0;
	margin-top: 0;
	padding: 0;
}

.list_item .uploader {
	font-weight: bold;
	margin-bottom: 0;
	margin-top: 0;
	padding: 0;
}

.list_item .title a {
	color: #990000;
}

.list_item .desc {
	padding-top: 0;
	padding-bottom: 0;
	padding-left: 0px;
	padding-right: 0px;
	margin-top: 0px;
	margin-bottom: 0;
}

div.tag_cloud {
	text-align: center;
	line-height: 1.6;
}

div.tag_cloud a {
	color: #990000;
}

div.workflow {
	text-align: center;
	margin: 6px;
	border-width: 1px solid #333333;
}

.workflow .info {
	text-align: center;
	line-height: 1.6;
	color: #333333;
}

.workflow .title {
	text-align: center;
	line-height: 1.0;
	color: #333333;
	font-size: 18pt;
	font-weight: bold;
	margin-bottom: 0;
	margin-top: 0;
	padding: 0;
}

.workflow img.preview {
	padding: 5px;
}

.workflow .desc {
	line-height: 1.4;
	/*background-color: #EEEEEE;*/
	text-align: left;
	width: 400px;
	font-size: 12pt;
	padding-left: 12px;
	padding-right: 12px;
	padding-top: 0;
	padding-bottom: 0;
}

.workflow .desc p {
	padding: 0;
}

.workflow .tags {
	text-align: center;
	width: 400px;
	line-height: 1.6;
}

.workflow .tags a {
	color: #990000;
}

.workflow .credits {
	text-align: center;
	width: 400px;
	line-height: 1.6;
}


div.file {
	text-align: center;
	margin: 6px;
	border-width: 1px solid #333333;
}

.file .info {
	text-align: center;
	line-height: 1.6;
	color: #333333;
}

.file .title {
	text-align: center;
	line-height: 1.0;
	color: #333333;
	font-size: 18pt;
	font-weight: bold;
	margin-bottom: 0;
	margin-top: 0;
	padding: 0;
}

.file img.preview {
	padding: 5px;
}

.file .desc {
	line-height: 1.4;
	/*background-color: #EEEEEE;*/
	text-align: left;
	width: 400px;
	font-size: 12pt;
	padding-left: 12px;
	padding-right: 12px;
	padding-top: 0;
	padding-bottom: 0;
}

.file .desc p {
	padding: 0;
}

.file .tags {
	text-align: center;
	width: 400px;
	line-height: 1.6;
}

.file .tags a {
	color: #990000;
}

.file .credits {
	text-align: center;
	width: 400px;
	line-height: 1.6;
}


div.pack {
	text-align: center;
	margin: 6px;
	border-width: 1px solid #333333;
}

.pack .info {
	text-align: center;
	line-height: 1.6;
	color: #333333;
}

.pack .title {
	text-align: center;
	line-height: 1.0;
	color: #333333;
	font-size: 18pt;
	font-weight: bold;
	margin-bottom: 0;
	margin-top: 0;
	padding: 0;
}

.pack img.preview {
	padding: 5px;
}

.pack .desc {
	line-height: 1.4;
	text-align: left;
	width: 400px;
	font-size: 12pt;
	padding-left: 12px;
	padding-right: 12px;
	padding-top: 0;
	padding-bottom: 0;
}

.pack .desc p {
	padding: 0;
}

.pack .tags {
	text-align: center;
	width: 400px;
	line-height: 1.6;
}

.pack .tags a {
	color: #990000;
}

.group .credits {
	text-align: center;
	width: 400px;
	line-height: 1.6;
}


div.group {
	text-align: center;
	margin: 6px;
	border-width: 1px solid #333333;
}

.group .info {
	text-align: center;
	line-height: 1.6;
	color: #333333;
}

.group .title {
	text-align: center;
	line-height: 1.0;
	color: #333333;
	font-size: 18pt;
	font-weight: bold;
	margin-bottom: 0;
	margin-top: 0;
	padding: 0;
}

.group img.preview {
	padding: 5px;
}

.group .desc {
	line-height: 1.4;
	text-align: left;
	width: 400px;
	font-size: 12pt;
	padding-left: 12px;
	padding-right: 12px;
	padding-top: 0;
	padding-bottom: 0;
}

.group .desc p {
	padding: 0;
}

.group .tags {
	text-align: center;
	width: 400px;
	line-height: 1.6;
}

.group .tags a {
	color: #990000;
}

.group .credits {
	text-align: center;
	width: 400px;
	line-height: 1.6;
}


div.user {
	text-align: center;
	margin: 6px;
	border-width: 1px solid #333333;
}

.user .info {
	text-align: center;
	line-height: 1.6;
	color: #333333;
}

.user .name {
	text-align: center;
	line-height: 1.0;
	color: #333333;
	font-size: 18pt;
	font-weight: bold;
	margin-bottom: 0;
	margin-top: 0;
	padding: 0;
}

.user img.avatar {
	padding: 5px;
}

.user .desc {
	line-height: 1.4;
	text-align: left;
	width: 400px;
	font-size: 12pt;
	padding-left: 12px;
	padding-right: 12px;
	padding-top: 0;
	padding-bottom: 0;
}

.user .desc p {
	padding: 0;
  width: 400px;
}

.user .contact_details_header {
  font-size: 12px;
  font-weight: bold;
  text-align: center;
  margin-top: 12px;
  margin-bottom: 6px;
}

.user .contact_details {
  text-align: left;
  margin-left: 100px;
  line-height: 1.5;
}

.user .items_header {
  font-size: 12px;
  font-weight: bold;
  text-align: center;
  margin-top: 22px;
  margin-bottom: 6px;
}

.none_text {
	color: #666666;
	font-style: italic;
}
</style>