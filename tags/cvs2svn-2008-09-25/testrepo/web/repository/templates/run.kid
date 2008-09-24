<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:py="http://purl.org/kid/ns#"
    py:extends="'master.kid'">

<head>
    <meta content="text/html; charset=UTF-8" http-equiv="content-type" py:replace="''"/>
    <title>Run of workflow $name</title>
</head>

<body>

    <h1>Run of workflow $name</h1>

    <div py:if="executed.outputs" class="workflow output outputs">
        <h2>Output</h2> 
        <div py:for="name,data in executed.outputs.items()">
            <h3>$name</h3>
            <div class="data" py:def="output_data(data)">
                <pre py:content="repr(data)">
                    Data
                </pre>
            </div>
            <div py:replace="output_data(data)" />
        </div>
    </div>

    <div py:if="executed.stdout" class="workflow stdout">
        <h2>Stdout</h2>
        <pre py:content="executed.stdout">
            Standard output
        </pre>
    </div>
    <div py:if="executed.stderr" class="workflow stderr">
        <h2>Stderr</h2>
        <pre py:content="executed.stderr">
            Standard Error
        </pre>
    </div>
    <div py:if="executed.report" class="workflow report">
        <h2>Report</h2>
        <pre py:content="executed.report">
            Report (XML)
        </pre>
    </div>

    <div class="workflow actions">
        <a href="${tg.url('/workflow/%s' % id)}">Repository info</a>
    </div>

</body>
</html>
