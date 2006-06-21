<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<?python import sitetemplate ?>
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:py="http://purl.org/kid/ns#" py:extends="sitetemplate">

<head py:match="item.tag=='{http://www.w3.org/1999/xhtml}head'" py:attrs="item.items()">
    <meta content="text/html; charset=UTF-8" http-equiv="content-type" py:replace="''"/>
    <title py:replace="''">Your title goes here</title>
    <meta py:replace="item[:]"/>
    <style type="text/css"><!--
        #pageLogin
        {
            font-size: 10px;
            font-family: verdana;
            text-align: right;
        }
        .actions {
            padding: 0.2em;
            height: 1.6em;
            margin-top: 0.4em;
        }
        .actions a {
            background: #ddd;
            padding: 0.3em;
            color: black;
            text-decoration: none; 
            border: black solid 1px;
            border-right: black solid 1px;
            margin-right: 1em;
        }
        .actions a:hover {   
            color: black;
            background: white;
        }
        .general {
            margin-top: 1.5em;
            padding-top: 1em;
            border-top: black solid 1px;
        }
        -->
    </style>
</head>

<body py:match="item.tag=='{http://www.w3.org/1999/xhtml}body'" py:attrs="item.items()">
    <div py:if="tg.config('identity.on',False) and not 'logging_in' in locals()"
        id="pageLogin">
        <span py:if="tg.identity.anonymous">
            <a href="/login">Login</a>
        </span>
        <span py:if="not tg.identity.anonymous">
            Welcome ${tg.identity.user.display_name}.
            <a href="/logout">Logout</a>
        </span>
    </div>

    <div py:if="tg_flash" class="flash" py:content="tg_flash"></div>

        <div py:replace="[item.text]+item[:]"/>

    <div class="general actions">
        <a href="${tg.url('/')}">Workflows</a>
        <a href="${tg.url('/add_workflow')}">Add workflow</a>
        <a href="${tg.url('/runs')}">All test runs</a>
    </div>

    <p align="center"><img src="/static/images/tg_under_the_hood.png" alt="TurboGears under the hood"/></p>
</body>

</html>
