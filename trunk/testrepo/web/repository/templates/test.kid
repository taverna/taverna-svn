<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:py="http://purl.org/kid/ns#"
    py:extends="'master.kid'">

<head>
    <meta content="text/html; charset=UTF-8" http-equiv="content-type" py:replace="''"/>
    <title>Workflow test for $workflow.name</title>
</head>

<body>

    <h1>Workflow test for $workflow.name</h1>

    <div class="workflow scufl port input">
        <h2>Workflow input port values</h2>
        <dl>
            <div py:for="input in test.inputs">
                <dt>$input.port</dt>
                <dd><pre py:content="input.data">Data</pre></dd>
            </div>
        </dl>
    </div>
    <div class="workflow scufl port output">
        <h2>Expected workflow outputs</h2>
        <dl>
            <div py:for="input in test.outputs">
                <dt>$input.port</dt>
                <dd><pre py:content="input.data">Data</pre></dd>
            </div>
        </dl>
    </div>

    <div class="workflow actions">
        <a href="${tg.url('/run_test/%s' % id)}">Run</a>
        <a href="${tg.url('/edit_test/%s' % id)}">Edit</a>
         <a href="${tg.url('/del_test/%s' % id)}">Delete</a>
        <a href="${tg.url('/workflow/%s' % workflow.id)}">View workflow</a>
    </div>

</body>
</html>
