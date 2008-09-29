<?xml version="1.0"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xlink="http://www.w3.org/1999/xlink"
    xmlns:tav="http://taverna.sf.net/service" version="1.0">

  <xsl:output method="html"/>

  <xsl:template match="*"/>

  <xsl:template match="tav:capabilities">
    <html>
      <head>
        <title>
            Capabilities
        </title>
        <link rel="stylesheet" type="text/css" href="/html/taverna.css" />
      </head>
      <body>
        <h1>Capabilities</h1>
        <div class="capabilities">
          <xsl:apply-templates/>
        </div>
      </body>
    </html>
  </xsl:template>

  <xsl:template match="tav:user">
      <div>Users</div>
  </xsl:template>

  <xsl:template match="tav:jobs">
      <div class="jobs">
          <h2>Jobs</h2>
          <xsl:apply-templates/>
      </div>
  </xsl:template>

  <xsl:template match="tav:user">
      <div class="user">
          <h2>User <xsl:value-of select="tav:username" /></h2>
          <dl>
            <xsl:apply-templates/>
          </dl>
      </div>
  </xsl:template>
  
  <xsl:template match="tav:job">
  	<div class="job">
  		<h2>Job</h2>
  		<dl>
  			<xsl:apply-templates />
  		</dl>
  	</div>
  </xsl:template>

  <xsl:template match="tav:status">
  	<dt>Status: </dt>
  	<dd>
  		<xsl:apply-templates />
  	</dd>
  </xsl:template>
  
  <xsl:template match="tav:owner">
  	<dt>Owner : </dt>
  	<dd>
  		<xsl:apply-templates />
  	</dd>
  </xsl:template>
  
    <xsl:template match="tav:workflow">
  	<div class="workflow">
  		<h2>Workflow</h2>
  		<dl>
  			<xsl:apply-templates />
  		</dl>
  	</div>
  </xsl:template>
  

  <xsl:template match="tav:username">
      <dt>Username</dt>
      <dd>
        <xsl:value-of select="." />
      </dd>
  </xsl:template>
  <xsl:template match="tav:email">
      <dt>E-mail</dt>
      <dd>
        <a href="mailto:{.}">
            <xsl:value-of select="." />
        </a>
      </dd>
  </xsl:template>

  <xsl:template match="tav:currentUser">
      <div>Current user</div>
  </xsl:template>

  <xsl:template match="tav:queues">
      <div class="queues">
          <h2>Queues</h2>
          <xsl:apply-templates/>
      </div>
  </xsl:template>

  <xsl:template match="tav:workers">
      <div class="workers">
          <h2>Workers</h2>
          <xsl:apply-templates/>
      </div>
  </xsl:template>

  <xsl:template match="tav:workflows">
      <div class="workflows">
          <h2>Workflows</h2>
          <xsl:apply-templates/>
      </div>
  </xsl:template>

  <xsl:template match="tav:datas">
      <div class="datas">
          <h2>Datas</h2>
          <xsl:apply-templates/>
      </div>
  </xsl:template>

  <xsl:template match="*[@xlink:href]">
      <div>
      <a href="{@xlink:href}">
          <xsl:value-of select="local-name()" />
          <xsl:apply-templates />
      </a>
    </div>
  </xsl:template>

</xsl:stylesheet>
