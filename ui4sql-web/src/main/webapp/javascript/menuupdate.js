/*
 * UI4SQL V1.0 (https://ui4sql.net)
 * Copyright 2022 PaulsenITSolutions 
 * Licensed under MIT (http://github.com/arnepaulsen/ui4sql/LICENSE) 
 */

    function UpdateIt(){
    if (ie&&keepstatic&&!opr6)
    document.all["MainTable"].style.top = document.body.scrollTop;
    setTimeout("UpdateIt()", 200);
    }
    UpdateIt();