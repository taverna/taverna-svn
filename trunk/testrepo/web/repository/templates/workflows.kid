<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:py="http://purl.org/kid/ns#"
    py:extends="'master.kid'">

<head>
    <meta content="text/html; charset=UTF-8" http-equiv="content-type" py:replace="''"/>
    <title>Workflows</title>
</head>

<body>

    <h1>Workflows</h1>
    
    <ul>
       <li py:for="wf_id,wf_name in workflows">
         <a
           href="${tg.url('/workflow/%d' % wf_id)}"
           py:content="wf_name">Workflow</a>
         (<a href="${tg.url('/download/%d' % wf_id)}">Download</a>)
       </li>
    </ul>

</body>
</html>
