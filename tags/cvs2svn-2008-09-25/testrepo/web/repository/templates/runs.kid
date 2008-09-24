<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:py="http://purl.org/kid/ns#"
    py:extends="'master.kid'">

<head>
    <meta content="text/html; charset=UTF-8" http-equiv="content-type" py:replace="''"/>
    <title>Workflow test runs</title>
    <style type="text/css">
    <!--
    .error {
        text-align: right;
    }
    .error0 {
        background: #afa;
        color: black;
    }
    .error1 {
        background: #cea;
        color: black;
    }
    .error2 {
        background: #dda;
        color: black;
    }
    .error3 {
        background: #dca;
        color: black;
    }
    .error4 {
        background: #eba;
        color: black;
    }
    .error5 {
        background: #faa;
        color: black;
    }
    .passed {
        font-size: smaller;
    }
    -->
    </style>
</head>

<body>

    <h1>Workflow test runs</h1>
    
    <table class="testruns">
       <tr><th>Errors</th> <th>Passed tests</th> <th>Workflow test run</th></tr>
       <tr py:for="errors,passed,name,run in runs">
        <td class="error error$errors">$errors</td>
        <td class="passed" py:content="' '.join(passed)">tests passed</td>
        <td class="workflow"><a href="${tg.url('/test_run/%s' % run.id)}"
             py:content="'%s %s %s %s' % (name, run.taverna, run.java,
             run.run_date.strftime('%Y-%m-%d'))">
             Workflow name</a></td>
       </tr>
    </table>
</body>
</html>
