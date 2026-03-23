/*
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
$(document).ready(function() {

    $(".click-title").mouseenter( function(    e){
        e.preventDefault();
        this.style.cursor="pointer";
    });
    $(".click-title").mousedown( function(event){
        event.preventDefault();
    });

    // Ugly code while this script is shared among several pages
    try{
        refreshHitsPerSecond(true);
    } catch(e){}
    try{
        refreshResponseTimeOverTime(true);
    } catch(e){}
    try{
        refreshResponseTimePercentiles();
    } catch(e){}
});


var responseTimePercentilesInfos = {
        data: {"result": {"minY": 107.0, "minX": 0.0, "maxY": 6507.0, "series": [{"data": [[0.0, 107.0], [0.1, 107.0], [0.2, 107.0], [0.3, 107.0], [0.4, 107.0], [0.5, 107.0], [0.6, 107.0], [0.7, 107.0], [0.8, 107.0], [0.9, 107.0], [1.0, 115.0], [1.1, 115.0], [1.2, 115.0], [1.3, 115.0], [1.4, 115.0], [1.5, 115.0], [1.6, 115.0], [1.7, 115.0], [1.8, 115.0], [1.9, 115.0], [2.0, 118.0], [2.1, 118.0], [2.2, 118.0], [2.3, 118.0], [2.4, 118.0], [2.5, 118.0], [2.6, 118.0], [2.7, 118.0], [2.8, 118.0], [2.9, 118.0], [3.0, 119.0], [3.1, 119.0], [3.2, 119.0], [3.3, 119.0], [3.4, 119.0], [3.5, 119.0], [3.6, 119.0], [3.7, 119.0], [3.8, 119.0], [3.9, 119.0], [4.0, 124.0], [4.1, 124.0], [4.2, 124.0], [4.3, 124.0], [4.4, 124.0], [4.5, 124.0], [4.6, 124.0], [4.7, 124.0], [4.8, 124.0], [4.9, 124.0], [5.0, 127.0], [5.1, 127.0], [5.2, 127.0], [5.3, 127.0], [5.4, 127.0], [5.5, 127.0], [5.6, 127.0], [5.7, 127.0], [5.8, 127.0], [5.9, 127.0], [6.0, 130.0], [6.1, 130.0], [6.2, 130.0], [6.3, 130.0], [6.4, 130.0], [6.5, 130.0], [6.6, 130.0], [6.7, 130.0], [6.8, 130.0], [6.9, 130.0], [7.0, 131.0], [7.1, 131.0], [7.2, 131.0], [7.3, 131.0], [7.4, 131.0], [7.5, 131.0], [7.6, 131.0], [7.7, 131.0], [7.8, 131.0], [7.9, 131.0], [8.0, 132.0], [8.1, 132.0], [8.2, 132.0], [8.3, 132.0], [8.4, 132.0], [8.5, 132.0], [8.6, 132.0], [8.7, 132.0], [8.8, 132.0], [8.9, 132.0], [9.0, 133.0], [9.1, 133.0], [9.2, 133.0], [9.3, 133.0], [9.4, 133.0], [9.5, 133.0], [9.6, 133.0], [9.7, 133.0], [9.8, 133.0], [9.9, 133.0], [10.0, 135.0], [10.1, 135.0], [10.2, 135.0], [10.3, 135.0], [10.4, 135.0], [10.5, 135.0], [10.6, 135.0], [10.7, 135.0], [10.8, 135.0], [10.9, 135.0], [11.0, 135.0], [11.1, 135.0], [11.2, 135.0], [11.3, 135.0], [11.4, 135.0], [11.5, 135.0], [11.6, 135.0], [11.7, 135.0], [11.8, 135.0], [11.9, 135.0], [12.0, 135.0], [12.1, 135.0], [12.2, 135.0], [12.3, 135.0], [12.4, 135.0], [12.5, 135.0], [12.6, 135.0], [12.7, 135.0], [12.8, 135.0], [12.9, 135.0], [13.0, 135.0], [13.1, 135.0], [13.2, 135.0], [13.3, 135.0], [13.4, 135.0], [13.5, 135.0], [13.6, 135.0], [13.7, 135.0], [13.8, 135.0], [13.9, 135.0], [14.0, 136.0], [14.1, 136.0], [14.2, 136.0], [14.3, 136.0], [14.4, 136.0], [14.5, 136.0], [14.6, 136.0], [14.7, 136.0], [14.8, 136.0], [14.9, 136.0], [15.0, 137.0], [15.1, 137.0], [15.2, 137.0], [15.3, 137.0], [15.4, 137.0], [15.5, 137.0], [15.6, 137.0], [15.7, 137.0], [15.8, 137.0], [15.9, 137.0], [16.0, 138.0], [16.1, 138.0], [16.2, 138.0], [16.3, 138.0], [16.4, 138.0], [16.5, 138.0], [16.6, 138.0], [16.7, 138.0], [16.8, 138.0], [16.9, 138.0], [17.0, 138.0], [17.1, 138.0], [17.2, 138.0], [17.3, 138.0], [17.4, 138.0], [17.5, 138.0], [17.6, 138.0], [17.7, 138.0], [17.8, 138.0], [17.9, 138.0], [18.0, 139.0], [18.1, 139.0], [18.2, 139.0], [18.3, 139.0], [18.4, 139.0], [18.5, 139.0], [18.6, 139.0], [18.7, 139.0], [18.8, 139.0], [18.9, 139.0], [19.0, 140.0], [19.1, 140.0], [19.2, 140.0], [19.3, 140.0], [19.4, 140.0], [19.5, 140.0], [19.6, 140.0], [19.7, 140.0], [19.8, 140.0], [19.9, 140.0], [20.0, 141.0], [20.1, 141.0], [20.2, 141.0], [20.3, 141.0], [20.4, 141.0], [20.5, 141.0], [20.6, 141.0], [20.7, 141.0], [20.8, 141.0], [20.9, 141.0], [21.0, 142.0], [21.1, 142.0], [21.2, 142.0], [21.3, 142.0], [21.4, 142.0], [21.5, 142.0], [21.6, 142.0], [21.7, 142.0], [21.8, 142.0], [21.9, 142.0], [22.0, 143.0], [22.1, 143.0], [22.2, 143.0], [22.3, 143.0], [22.4, 143.0], [22.5, 143.0], [22.6, 143.0], [22.7, 143.0], [22.8, 143.0], [22.9, 143.0], [23.0, 143.0], [23.1, 143.0], [23.2, 143.0], [23.3, 143.0], [23.4, 143.0], [23.5, 143.0], [23.6, 143.0], [23.7, 143.0], [23.8, 143.0], [23.9, 143.0], [24.0, 144.0], [24.1, 144.0], [24.2, 144.0], [24.3, 144.0], [24.4, 144.0], [24.5, 144.0], [24.6, 144.0], [24.7, 144.0], [24.8, 144.0], [24.9, 144.0], [25.0, 145.0], [25.1, 145.0], [25.2, 145.0], [25.3, 145.0], [25.4, 145.0], [25.5, 145.0], [25.6, 145.0], [25.7, 145.0], [25.8, 145.0], [25.9, 145.0], [26.0, 146.0], [26.1, 146.0], [26.2, 146.0], [26.3, 146.0], [26.4, 146.0], [26.5, 146.0], [26.6, 146.0], [26.7, 146.0], [26.8, 146.0], [26.9, 146.0], [27.0, 146.0], [27.1, 146.0], [27.2, 146.0], [27.3, 146.0], [27.4, 146.0], [27.5, 146.0], [27.6, 146.0], [27.7, 146.0], [27.8, 146.0], [27.9, 146.0], [28.0, 150.0], [28.1, 150.0], [28.2, 150.0], [28.3, 150.0], [28.4, 150.0], [28.5, 150.0], [28.6, 150.0], [28.7, 150.0], [28.8, 150.0], [28.9, 150.0], [29.0, 151.0], [29.1, 151.0], [29.2, 151.0], [29.3, 151.0], [29.4, 151.0], [29.5, 151.0], [29.6, 151.0], [29.7, 151.0], [29.8, 151.0], [29.9, 151.0], [30.0, 151.0], [30.1, 151.0], [30.2, 151.0], [30.3, 151.0], [30.4, 151.0], [30.5, 151.0], [30.6, 151.0], [30.7, 151.0], [30.8, 151.0], [30.9, 151.0], [31.0, 153.0], [31.1, 153.0], [31.2, 153.0], [31.3, 153.0], [31.4, 153.0], [31.5, 153.0], [31.6, 153.0], [31.7, 153.0], [31.8, 153.0], [31.9, 153.0], [32.0, 153.0], [32.1, 153.0], [32.2, 153.0], [32.3, 153.0], [32.4, 153.0], [32.5, 153.0], [32.6, 153.0], [32.7, 153.0], [32.8, 153.0], [32.9, 153.0], [33.0, 154.0], [33.1, 154.0], [33.2, 154.0], [33.3, 154.0], [33.4, 154.0], [33.5, 154.0], [33.6, 154.0], [33.7, 154.0], [33.8, 154.0], [33.9, 154.0], [34.0, 154.0], [34.1, 154.0], [34.2, 154.0], [34.3, 154.0], [34.4, 154.0], [34.5, 154.0], [34.6, 154.0], [34.7, 154.0], [34.8, 154.0], [34.9, 154.0], [35.0, 160.0], [35.1, 160.0], [35.2, 160.0], [35.3, 160.0], [35.4, 160.0], [35.5, 160.0], [35.6, 160.0], [35.7, 160.0], [35.8, 160.0], [35.9, 160.0], [36.0, 161.0], [36.1, 161.0], [36.2, 161.0], [36.3, 161.0], [36.4, 161.0], [36.5, 161.0], [36.6, 161.0], [36.7, 161.0], [36.8, 161.0], [36.9, 161.0], [37.0, 163.0], [37.1, 163.0], [37.2, 163.0], [37.3, 163.0], [37.4, 163.0], [37.5, 163.0], [37.6, 163.0], [37.7, 163.0], [37.8, 163.0], [37.9, 163.0], [38.0, 165.0], [38.1, 165.0], [38.2, 165.0], [38.3, 165.0], [38.4, 165.0], [38.5, 165.0], [38.6, 165.0], [38.7, 165.0], [38.8, 165.0], [38.9, 165.0], [39.0, 166.0], [39.1, 166.0], [39.2, 166.0], [39.3, 166.0], [39.4, 166.0], [39.5, 166.0], [39.6, 166.0], [39.7, 166.0], [39.8, 166.0], [39.9, 166.0], [40.0, 169.0], [40.1, 169.0], [40.2, 169.0], [40.3, 169.0], [40.4, 169.0], [40.5, 169.0], [40.6, 169.0], [40.7, 169.0], [40.8, 169.0], [40.9, 169.0], [41.0, 171.0], [41.1, 171.0], [41.2, 171.0], [41.3, 171.0], [41.4, 171.0], [41.5, 171.0], [41.6, 171.0], [41.7, 171.0], [41.8, 171.0], [41.9, 171.0], [42.0, 178.0], [42.1, 178.0], [42.2, 178.0], [42.3, 178.0], [42.4, 178.0], [42.5, 178.0], [42.6, 178.0], [42.7, 178.0], [42.8, 178.0], [42.9, 178.0], [43.0, 178.0], [43.1, 178.0], [43.2, 178.0], [43.3, 178.0], [43.4, 178.0], [43.5, 178.0], [43.6, 178.0], [43.7, 178.0], [43.8, 178.0], [43.9, 178.0], [44.0, 180.0], [44.1, 180.0], [44.2, 180.0], [44.3, 180.0], [44.4, 180.0], [44.5, 180.0], [44.6, 180.0], [44.7, 180.0], [44.8, 180.0], [44.9, 180.0], [45.0, 180.0], [45.1, 180.0], [45.2, 180.0], [45.3, 180.0], [45.4, 180.0], [45.5, 180.0], [45.6, 180.0], [45.7, 180.0], [45.8, 180.0], [45.9, 180.0], [46.0, 181.0], [46.1, 181.0], [46.2, 181.0], [46.3, 181.0], [46.4, 181.0], [46.5, 181.0], [46.6, 181.0], [46.7, 181.0], [46.8, 181.0], [46.9, 181.0], [47.0, 188.0], [47.1, 188.0], [47.2, 188.0], [47.3, 188.0], [47.4, 188.0], [47.5, 188.0], [47.6, 188.0], [47.7, 188.0], [47.8, 188.0], [47.9, 188.0], [48.0, 189.0], [48.1, 189.0], [48.2, 189.0], [48.3, 189.0], [48.4, 189.0], [48.5, 189.0], [48.6, 189.0], [48.7, 189.0], [48.8, 189.0], [48.9, 189.0], [49.0, 192.0], [49.1, 192.0], [49.2, 192.0], [49.3, 192.0], [49.4, 192.0], [49.5, 192.0], [49.6, 192.0], [49.7, 192.0], [49.8, 192.0], [49.9, 192.0], [50.0, 198.0], [50.1, 198.0], [50.2, 198.0], [50.3, 198.0], [50.4, 198.0], [50.5, 198.0], [50.6, 198.0], [50.7, 198.0], [50.8, 198.0], [50.9, 198.0], [51.0, 199.0], [51.1, 199.0], [51.2, 199.0], [51.3, 199.0], [51.4, 199.0], [51.5, 199.0], [51.6, 199.0], [51.7, 199.0], [51.8, 199.0], [51.9, 199.0], [52.0, 213.0], [52.1, 213.0], [52.2, 213.0], [52.3, 213.0], [52.4, 213.0], [52.5, 213.0], [52.6, 213.0], [52.7, 213.0], [52.8, 213.0], [52.9, 213.0], [53.0, 216.0], [53.1, 216.0], [53.2, 216.0], [53.3, 216.0], [53.4, 216.0], [53.5, 216.0], [53.6, 216.0], [53.7, 216.0], [53.8, 216.0], [53.9, 216.0], [54.0, 245.0], [54.1, 245.0], [54.2, 245.0], [54.3, 245.0], [54.4, 245.0], [54.5, 245.0], [54.6, 245.0], [54.7, 245.0], [54.8, 245.0], [54.9, 245.0], [55.0, 258.0], [55.1, 258.0], [55.2, 258.0], [55.3, 258.0], [55.4, 258.0], [55.5, 258.0], [55.6, 258.0], [55.7, 258.0], [55.8, 258.0], [55.9, 258.0], [56.0, 275.0], [56.1, 275.0], [56.2, 275.0], [56.3, 275.0], [56.4, 275.0], [56.5, 275.0], [56.6, 275.0], [56.7, 275.0], [56.8, 275.0], [56.9, 275.0], [57.0, 288.0], [57.1, 288.0], [57.2, 288.0], [57.3, 288.0], [57.4, 288.0], [57.5, 288.0], [57.6, 288.0], [57.7, 288.0], [57.8, 288.0], [57.9, 288.0], [58.0, 335.0], [58.1, 335.0], [58.2, 335.0], [58.3, 335.0], [58.4, 335.0], [58.5, 335.0], [58.6, 335.0], [58.7, 335.0], [58.8, 335.0], [58.9, 335.0], [59.0, 350.0], [59.1, 350.0], [59.2, 350.0], [59.3, 350.0], [59.4, 350.0], [59.5, 350.0], [59.6, 350.0], [59.7, 350.0], [59.8, 350.0], [59.9, 350.0], [60.0, 360.0], [60.1, 360.0], [60.2, 360.0], [60.3, 360.0], [60.4, 360.0], [60.5, 360.0], [60.6, 360.0], [60.7, 360.0], [60.8, 360.0], [60.9, 360.0], [61.0, 364.0], [61.1, 364.0], [61.2, 364.0], [61.3, 364.0], [61.4, 364.0], [61.5, 364.0], [61.6, 364.0], [61.7, 364.0], [61.8, 364.0], [61.9, 364.0], [62.0, 388.0], [62.1, 388.0], [62.2, 388.0], [62.3, 388.0], [62.4, 388.0], [62.5, 388.0], [62.6, 388.0], [62.7, 388.0], [62.8, 388.0], [62.9, 388.0], [63.0, 403.0], [63.1, 403.0], [63.2, 403.0], [63.3, 403.0], [63.4, 403.0], [63.5, 403.0], [63.6, 403.0], [63.7, 403.0], [63.8, 403.0], [63.9, 403.0], [64.0, 436.0], [64.1, 436.0], [64.2, 436.0], [64.3, 436.0], [64.4, 436.0], [64.5, 436.0], [64.6, 436.0], [64.7, 436.0], [64.8, 436.0], [64.9, 436.0], [65.0, 454.0], [65.1, 454.0], [65.2, 454.0], [65.3, 454.0], [65.4, 454.0], [65.5, 454.0], [65.6, 454.0], [65.7, 454.0], [65.8, 454.0], [65.9, 454.0], [66.0, 467.0], [66.1, 467.0], [66.2, 467.0], [66.3, 467.0], [66.4, 467.0], [66.5, 467.0], [66.6, 467.0], [66.7, 467.0], [66.8, 467.0], [66.9, 467.0], [67.0, 482.0], [67.1, 482.0], [67.2, 482.0], [67.3, 482.0], [67.4, 482.0], [67.5, 482.0], [67.6, 482.0], [67.7, 482.0], [67.8, 482.0], [67.9, 482.0], [68.0, 483.0], [68.1, 483.0], [68.2, 483.0], [68.3, 483.0], [68.4, 483.0], [68.5, 483.0], [68.6, 483.0], [68.7, 483.0], [68.8, 483.0], [68.9, 483.0], [69.0, 487.0], [69.1, 487.0], [69.2, 487.0], [69.3, 487.0], [69.4, 487.0], [69.5, 487.0], [69.6, 487.0], [69.7, 487.0], [69.8, 487.0], [69.9, 487.0], [70.0, 487.0], [70.1, 487.0], [70.2, 487.0], [70.3, 487.0], [70.4, 487.0], [70.5, 487.0], [70.6, 487.0], [70.7, 487.0], [70.8, 487.0], [70.9, 487.0], [71.0, 499.0], [71.1, 499.0], [71.2, 499.0], [71.3, 499.0], [71.4, 499.0], [71.5, 499.0], [71.6, 499.0], [71.7, 499.0], [71.8, 499.0], [71.9, 499.0], [72.0, 501.0], [72.1, 501.0], [72.2, 501.0], [72.3, 501.0], [72.4, 501.0], [72.5, 501.0], [72.6, 501.0], [72.7, 501.0], [72.8, 501.0], [72.9, 501.0], [73.0, 507.0], [73.1, 507.0], [73.2, 507.0], [73.3, 507.0], [73.4, 507.0], [73.5, 507.0], [73.6, 507.0], [73.7, 507.0], [73.8, 507.0], [73.9, 507.0], [74.0, 529.0], [74.1, 529.0], [74.2, 529.0], [74.3, 529.0], [74.4, 529.0], [74.5, 529.0], [74.6, 529.0], [74.7, 529.0], [74.8, 529.0], [74.9, 529.0], [75.0, 532.0], [75.1, 532.0], [75.2, 532.0], [75.3, 532.0], [75.4, 532.0], [75.5, 532.0], [75.6, 532.0], [75.7, 532.0], [75.8, 532.0], [75.9, 532.0], [76.0, 533.0], [76.1, 533.0], [76.2, 533.0], [76.3, 533.0], [76.4, 533.0], [76.5, 533.0], [76.6, 533.0], [76.7, 533.0], [76.8, 533.0], [76.9, 533.0], [77.0, 549.0], [77.1, 549.0], [77.2, 549.0], [77.3, 549.0], [77.4, 549.0], [77.5, 549.0], [77.6, 549.0], [77.7, 549.0], [77.8, 549.0], [77.9, 549.0], [78.0, 552.0], [78.1, 552.0], [78.2, 552.0], [78.3, 552.0], [78.4, 552.0], [78.5, 552.0], [78.6, 552.0], [78.7, 552.0], [78.8, 552.0], [78.9, 552.0], [79.0, 578.0], [79.1, 578.0], [79.2, 578.0], [79.3, 578.0], [79.4, 578.0], [79.5, 578.0], [79.6, 578.0], [79.7, 578.0], [79.8, 578.0], [79.9, 578.0], [80.0, 580.0], [80.1, 580.0], [80.2, 580.0], [80.3, 580.0], [80.4, 580.0], [80.5, 580.0], [80.6, 580.0], [80.7, 580.0], [80.8, 580.0], [80.9, 580.0], [81.0, 586.0], [81.1, 586.0], [81.2, 586.0], [81.3, 586.0], [81.4, 586.0], [81.5, 586.0], [81.6, 586.0], [81.7, 586.0], [81.8, 586.0], [81.9, 586.0], [82.0, 594.0], [82.1, 594.0], [82.2, 594.0], [82.3, 594.0], [82.4, 594.0], [82.5, 594.0], [82.6, 594.0], [82.7, 594.0], [82.8, 594.0], [82.9, 594.0], [83.0, 602.0], [83.1, 602.0], [83.2, 602.0], [83.3, 602.0], [83.4, 602.0], [83.5, 602.0], [83.6, 602.0], [83.7, 602.0], [83.8, 602.0], [83.9, 602.0], [84.0, 605.0], [84.1, 605.0], [84.2, 605.0], [84.3, 605.0], [84.4, 605.0], [84.5, 605.0], [84.6, 605.0], [84.7, 605.0], [84.8, 605.0], [84.9, 605.0], [85.0, 609.0], [85.1, 609.0], [85.2, 609.0], [85.3, 609.0], [85.4, 609.0], [85.5, 609.0], [85.6, 609.0], [85.7, 609.0], [85.8, 609.0], [85.9, 609.0], [86.0, 631.0], [86.1, 631.0], [86.2, 631.0], [86.3, 631.0], [86.4, 631.0], [86.5, 631.0], [86.6, 631.0], [86.7, 631.0], [86.8, 631.0], [86.9, 631.0], [87.0, 639.0], [87.1, 639.0], [87.2, 639.0], [87.3, 639.0], [87.4, 639.0], [87.5, 639.0], [87.6, 639.0], [87.7, 639.0], [87.8, 639.0], [87.9, 639.0], [88.0, 649.0], [88.1, 649.0], [88.2, 649.0], [88.3, 649.0], [88.4, 649.0], [88.5, 649.0], [88.6, 649.0], [88.7, 649.0], [88.8, 649.0], [88.9, 649.0], [89.0, 680.0], [89.1, 680.0], [89.2, 680.0], [89.3, 680.0], [89.4, 680.0], [89.5, 680.0], [89.6, 680.0], [89.7, 680.0], [89.8, 680.0], [89.9, 680.0], [90.0, 3538.0], [90.1, 3538.0], [90.2, 3538.0], [90.3, 3538.0], [90.4, 3538.0], [90.5, 3538.0], [90.6, 3538.0], [90.7, 3538.0], [90.8, 3538.0], [90.9, 3538.0], [91.0, 3603.0], [91.1, 3603.0], [91.2, 3603.0], [91.3, 3603.0], [91.4, 3603.0], [91.5, 3603.0], [91.6, 3603.0], [91.7, 3603.0], [91.8, 3603.0], [91.9, 3603.0], [92.0, 6156.0], [92.1, 6156.0], [92.2, 6156.0], [92.3, 6156.0], [92.4, 6156.0], [92.5, 6156.0], [92.6, 6156.0], [92.7, 6156.0], [92.8, 6156.0], [92.9, 6156.0], [93.0, 6207.0], [93.1, 6207.0], [93.2, 6207.0], [93.3, 6207.0], [93.4, 6207.0], [93.5, 6207.0], [93.6, 6207.0], [93.7, 6207.0], [93.8, 6207.0], [93.9, 6207.0], [94.0, 6259.0], [94.1, 6259.0], [94.2, 6259.0], [94.3, 6259.0], [94.4, 6259.0], [94.5, 6259.0], [94.6, 6259.0], [94.7, 6259.0], [94.8, 6259.0], [94.9, 6259.0], [95.0, 6308.0], [95.1, 6308.0], [95.2, 6308.0], [95.3, 6308.0], [95.4, 6308.0], [95.5, 6308.0], [95.6, 6308.0], [95.7, 6308.0], [95.8, 6308.0], [95.9, 6308.0], [96.0, 6358.0], [96.1, 6358.0], [96.2, 6358.0], [96.3, 6358.0], [96.4, 6358.0], [96.5, 6358.0], [96.6, 6358.0], [96.7, 6358.0], [96.8, 6358.0], [96.9, 6358.0], [97.0, 6380.0], [97.1, 6380.0], [97.2, 6380.0], [97.3, 6380.0], [97.4, 6380.0], [97.5, 6380.0], [97.6, 6380.0], [97.7, 6380.0], [97.8, 6380.0], [97.9, 6380.0], [98.0, 6452.0], [98.1, 6452.0], [98.2, 6452.0], [98.3, 6452.0], [98.4, 6452.0], [98.5, 6452.0], [98.6, 6452.0], [98.7, 6452.0], [98.8, 6452.0], [98.9, 6452.0], [99.0, 6507.0], [99.1, 6507.0], [99.2, 6507.0], [99.3, 6507.0], [99.4, 6507.0], [99.5, 6507.0], [99.6, 6507.0], [99.7, 6507.0], [99.8, 6507.0], [99.9, 6507.0]], "isOverall": false, "label": "参与秒杀", "isController": false}], "supportsControllersDiscrimination": true, "maxX": 100.0, "title": "Response Time Percentiles"}},
        getOptions: function() {
            return {
                series: {
                    points: { show: false }
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendResponseTimePercentiles'
                },
                xaxis: {
                    tickDecimals: 1,
                    axisLabel: "Percentiles",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Percentile value in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s : %x.2 percentile was %y ms"
                },
                selection: { mode: "xy" },
            };
        },
        createGraph: function() {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesResponseTimePercentiles"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotResponseTimesPercentiles"), dataset, options);
            // setup overview
            $.plot($("#overviewResponseTimesPercentiles"), dataset, prepareOverviewOptions(options));
        }
};

/**
 * @param elementId Id of element where we display message
 */
function setEmptyGraph(elementId) {
    $(function() {
        $(elementId).text("No graph series with filter="+seriesFilter);
    });
}

// Response times percentiles
function refreshResponseTimePercentiles() {
    var infos = responseTimePercentilesInfos;
    prepareSeries(infos.data);
    if(infos.data.result.series.length == 0) {
        setEmptyGraph("#bodyResponseTimePercentiles");
        return;
    }
    if (isGraph($("#flotResponseTimesPercentiles"))){
        infos.createGraph();
    } else {
        var choiceContainer = $("#choicesResponseTimePercentiles");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotResponseTimesPercentiles", "#overviewResponseTimesPercentiles");
        $('#bodyResponseTimePercentiles .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
}

var responseTimeDistributionInfos = {
        data: {"result": {"minY": 1.0, "minX": 100.0, "maxY": 52.0, "series": [{"data": [[600.0, 7.0], [200.0, 6.0], [3500.0, 1.0], [3600.0, 1.0], [300.0, 5.0], [6100.0, 1.0], [6300.0, 3.0], [6200.0, 2.0], [400.0, 9.0], [100.0, 52.0], [6500.0, 1.0], [6400.0, 1.0], [500.0, 11.0]], "isOverall": false, "label": "参与秒杀", "isController": false}], "supportsControllersDiscrimination": true, "granularity": 100, "maxX": 6500.0, "title": "Response Time Distribution"}},
        getOptions: function() {
            var granularity = this.data.result.granularity;
            return {
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendResponseTimeDistribution'
                },
                xaxis:{
                    axisLabel: "Response times in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Number of responses",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                bars : {
                    show: true,
                    barWidth: this.data.result.granularity
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: function(label, xval, yval, flotItem){
                        return yval + " responses for " + label + " were between " + xval + " and " + (xval + granularity) + " ms";
                    }
                }
            };
        },
        createGraph: function() {
            var data = this.data;
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotResponseTimeDistribution"), prepareData(data.result.series, $("#choicesResponseTimeDistribution")), options);
        }

};

// Response time distribution
function refreshResponseTimeDistribution() {
    var infos = responseTimeDistributionInfos;
    prepareSeries(infos.data);
    if(infos.data.result.series.length == 0) {
        setEmptyGraph("#bodyResponseTimeDistribution");
        return;
    }
    if (isGraph($("#flotResponseTimeDistribution"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesResponseTimeDistribution");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        $('#footerResponseTimeDistribution .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};


var syntheticResponseTimeDistributionInfos = {
        data: {"result": {"minY": 10.0, "minX": 2.0, "ticks": [[0, "Requests having \nresponse time <= 500ms"], [1, "Requests having \nresponse time > 500ms and <= 1,500ms"], [2, "Requests having \nresponse time > 1,500ms"], [3, "Requests in error"]], "maxY": 90.0, "series": [{"data": [], "color": "#9ACD32", "isOverall": false, "label": "Requests having \nresponse time <= 500ms", "isController": false}, {"data": [], "color": "yellow", "isOverall": false, "label": "Requests having \nresponse time > 500ms and <= 1,500ms", "isController": false}, {"data": [[2.0, 10.0]], "color": "orange", "isOverall": false, "label": "Requests having \nresponse time > 1,500ms", "isController": false}, {"data": [[3.0, 90.0]], "color": "#FF6347", "isOverall": false, "label": "Requests in error", "isController": false}], "supportsControllersDiscrimination": false, "maxX": 3.0, "title": "Synthetic Response Times Distribution"}},
        getOptions: function() {
            return {
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendSyntheticResponseTimeDistribution'
                },
                xaxis:{
                    axisLabel: "Response times ranges",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                    tickLength:0,
                    min:-0.5,
                    max:3.5
                },
                yaxis: {
                    axisLabel: "Number of responses",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                bars : {
                    show: true,
                    align: "center",
                    barWidth: 0.25,
                    fill:.75
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: function(label, xval, yval, flotItem){
                        return yval + " " + label;
                    }
                }
            };
        },
        createGraph: function() {
            var data = this.data;
            var options = this.getOptions();
            prepareOptions(options, data);
            options.xaxis.ticks = data.result.ticks;
            $.plot($("#flotSyntheticResponseTimeDistribution"), prepareData(data.result.series, $("#choicesSyntheticResponseTimeDistribution")), options);
        }

};

// Response time distribution
function refreshSyntheticResponseTimeDistribution() {
    var infos = syntheticResponseTimeDistributionInfos;
    prepareSeries(infos.data, true);
    if (isGraph($("#flotSyntheticResponseTimeDistribution"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesSyntheticResponseTimeDistribution");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        $('#footerSyntheticResponseTimeDistribution .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var activeThreadsOverTimeInfos = {
        data: {"result": {"minY": 14.879999999999999, "minX": 1.77406512E12, "maxY": 14.879999999999999, "series": [{"data": [[1.77406512E12, 14.879999999999999]], "isOverall": false, "label": "秒杀参与压测", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 60000, "maxX": 1.77406512E12, "title": "Active Threads Over Time"}},
        getOptions: function() {
            return {
                series: {
                    stack: true,
                    lines: {
                        show: true,
                        fill: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Number of active threads",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20
                },
                legend: {
                    noColumns: 6,
                    show: true,
                    container: '#legendActiveThreadsOverTime'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                selection: {
                    mode: 'xy'
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s : At %x there were %y active threads"
                }
            };
        },
        createGraph: function() {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesActiveThreadsOverTime"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotActiveThreadsOverTime"), dataset, options);
            // setup overview
            $.plot($("#overviewActiveThreadsOverTime"), dataset, prepareOverviewOptions(options));
        }
};

// Active Threads Over Time
function refreshActiveThreadsOverTime(fixTimestamps) {
    var infos = activeThreadsOverTimeInfos;
    prepareSeries(infos.data);
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, 28800000);
    }
    if(isGraph($("#flotActiveThreadsOverTime"))) {
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesActiveThreadsOverTime");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotActiveThreadsOverTime", "#overviewActiveThreadsOverTime");
        $('#footerActiveThreadsOverTime .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var timeVsThreadsInfos = {
        data: {"result": {"minY": 143.08333333333331, "minX": 8.0, "maxY": 6328.375, "series": [{"data": [[8.0, 6328.375], [10.0, 191.0], [11.0, 165.46666666666667], [12.0, 614.0666666666667], [13.0, 143.08333333333331], [14.0, 161.3846153846154], [15.0, 245.0], [16.0, 266.5], [17.0, 311.5], [18.0, 358.0], [19.0, 409.0], [20.0, 475.6], [21.0, 507.0], [22.0, 543.9], [23.0, 611.75], [24.0, 640.5]], "isOverall": false, "label": "参与秒杀", "isController": false}, {"data": [[14.879999999999999, 835.52]], "isOverall": false, "label": "参与秒杀-Aggregated", "isController": false}], "supportsControllersDiscrimination": true, "maxX": 24.0, "title": "Time VS Threads"}},
        getOptions: function() {
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    axisLabel: "Number of active threads",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Average response times in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20
                },
                legend: { noColumns: 2,show: true, container: '#legendTimeVsThreads' },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s: At %x.2 active threads, Average response time was %y.2 ms"
                }
            };
        },
        createGraph: function() {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesTimeVsThreads"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotTimesVsThreads"), dataset, options);
            // setup overview
            $.plot($("#overviewTimesVsThreads"), dataset, prepareOverviewOptions(options));
        }
};

// Time vs threads
function refreshTimeVsThreads(){
    var infos = timeVsThreadsInfos;
    prepareSeries(infos.data);
    if(infos.data.result.series.length == 0) {
        setEmptyGraph("#bodyTimeVsThreads");
        return;
    }
    if(isGraph($("#flotTimesVsThreads"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesTimeVsThreads");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotTimesVsThreads", "#overviewTimesVsThreads");
        $('#footerTimeVsThreads .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var bytesThroughputOverTimeInfos = {
        data : {"result": {"minY": 444.1666666666667, "minX": 1.77406512E12, "maxY": 751.4166666666666, "series": [{"data": [[1.77406512E12, 444.1666666666667]], "isOverall": false, "label": "Bytes received per second", "isController": false}, {"data": [[1.77406512E12, 751.4166666666666]], "isOverall": false, "label": "Bytes sent per second", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 60000, "maxX": 1.77406512E12, "title": "Bytes Throughput Over Time"}},
        getOptions : function(){
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity) ,
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Bytes / sec",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendBytesThroughputOverTime'
                },
                selection: {
                    mode: "xy"
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s at %x was %y"
                }
            };
        },
        createGraph : function() {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesBytesThroughputOverTime"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotBytesThroughputOverTime"), dataset, options);
            // setup overview
            $.plot($("#overviewBytesThroughputOverTime"), dataset, prepareOverviewOptions(options));
        }
};

// Bytes throughput Over Time
function refreshBytesThroughputOverTime(fixTimestamps) {
    var infos = bytesThroughputOverTimeInfos;
    prepareSeries(infos.data);
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, 28800000);
    }
    if(isGraph($("#flotBytesThroughputOverTime"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesBytesThroughputOverTime");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotBytesThroughputOverTime", "#overviewBytesThroughputOverTime");
        $('#footerBytesThroughputOverTime .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
}

var responseTimesOverTimeInfos = {
        data: {"result": {"minY": 835.52, "minX": 1.77406512E12, "maxY": 835.52, "series": [{"data": [[1.77406512E12, 835.52]], "isOverall": false, "label": "参与秒杀", "isController": false}], "supportsControllersDiscrimination": true, "granularity": 60000, "maxX": 1.77406512E12, "title": "Response Time Over Time"}},
        getOptions: function(){
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Average response time in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendResponseTimesOverTime'
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s : at %x Average response time was %y ms"
                }
            };
        },
        createGraph: function() {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesResponseTimesOverTime"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotResponseTimesOverTime"), dataset, options);
            // setup overview
            $.plot($("#overviewResponseTimesOverTime"), dataset, prepareOverviewOptions(options));
        }
};

// Response Times Over Time
function refreshResponseTimeOverTime(fixTimestamps) {
    var infos = responseTimesOverTimeInfos;
    prepareSeries(infos.data);
    if(infos.data.result.series.length == 0) {
        setEmptyGraph("#bodyResponseTimeOverTime");
        return;
    }
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, 28800000);
    }
    if(isGraph($("#flotResponseTimesOverTime"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesResponseTimesOverTime");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotResponseTimesOverTime", "#overviewResponseTimesOverTime");
        $('#footerResponseTimesOverTime .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var latenciesOverTimeInfos = {
        data: {"result": {"minY": 835.24, "minX": 1.77406512E12, "maxY": 835.24, "series": [{"data": [[1.77406512E12, 835.24]], "isOverall": false, "label": "参与秒杀", "isController": false}], "supportsControllersDiscrimination": true, "granularity": 60000, "maxX": 1.77406512E12, "title": "Latencies Over Time"}},
        getOptions: function() {
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Average response latencies in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendLatenciesOverTime'
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s : at %x Average latency was %y ms"
                }
            };
        },
        createGraph: function () {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesLatenciesOverTime"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotLatenciesOverTime"), dataset, options);
            // setup overview
            $.plot($("#overviewLatenciesOverTime"), dataset, prepareOverviewOptions(options));
        }
};

// Latencies Over Time
function refreshLatenciesOverTime(fixTimestamps) {
    var infos = latenciesOverTimeInfos;
    prepareSeries(infos.data);
    if(infos.data.result.series.length == 0) {
        setEmptyGraph("#bodyLatenciesOverTime");
        return;
    }
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, 28800000);
    }
    if(isGraph($("#flotLatenciesOverTime"))) {
        infos.createGraph();
    }else {
        var choiceContainer = $("#choicesLatenciesOverTime");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotLatenciesOverTime", "#overviewLatenciesOverTime");
        $('#footerLatenciesOverTime .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var connectTimeOverTimeInfos = {
        data: {"result": {"minY": 0.85, "minX": 1.77406512E12, "maxY": 0.85, "series": [{"data": [[1.77406512E12, 0.85]], "isOverall": false, "label": "参与秒杀", "isController": false}], "supportsControllersDiscrimination": true, "granularity": 60000, "maxX": 1.77406512E12, "title": "Connect Time Over Time"}},
        getOptions: function() {
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getConnectTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Average Connect Time in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendConnectTimeOverTime'
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s : at %x Average connect time was %y ms"
                }
            };
        },
        createGraph: function () {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesConnectTimeOverTime"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotConnectTimeOverTime"), dataset, options);
            // setup overview
            $.plot($("#overviewConnectTimeOverTime"), dataset, prepareOverviewOptions(options));
        }
};

// Connect Time Over Time
function refreshConnectTimeOverTime(fixTimestamps) {
    var infos = connectTimeOverTimeInfos;
    prepareSeries(infos.data);
    if(infos.data.result.series.length == 0) {
        setEmptyGraph("#bodyConnectTimeOverTime");
        return;
    }
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, 28800000);
    }
    if(isGraph($("#flotConnectTimeOverTime"))) {
        infos.createGraph();
    }else {
        var choiceContainer = $("#choicesConnectTimeOverTime");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotConnectTimeOverTime", "#overviewConnectTimeOverTime");
        $('#footerConnectTimeOverTime .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var responseTimePercentilesOverTimeInfos = {
        data: {"result": {"minY": 3538.0, "minX": 1.77406512E12, "maxY": 6507.0, "series": [{"data": [[1.77406512E12, 6507.0]], "isOverall": false, "label": "Max", "isController": false}, {"data": [[1.77406512E12, 3538.0]], "isOverall": false, "label": "Min", "isController": false}, {"data": [[1.77406512E12, 6501.5]], "isOverall": false, "label": "90th percentile", "isController": false}, {"data": [[1.77406512E12, 6507.0]], "isOverall": false, "label": "99th percentile", "isController": false}, {"data": [[1.77406512E12, 6283.5]], "isOverall": false, "label": "Median", "isController": false}, {"data": [[1.77406512E12, 6507.0]], "isOverall": false, "label": "95th percentile", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 60000, "maxX": 1.77406512E12, "title": "Response Time Percentiles Over Time (successful requests only)"}},
        getOptions: function() {
            return {
                series: {
                    lines: {
                        show: true,
                        fill: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Response Time in ms",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: '#legendResponseTimePercentilesOverTime'
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s : at %x Response time was %y ms"
                }
            };
        },
        createGraph: function () {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesResponseTimePercentilesOverTime"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotResponseTimePercentilesOverTime"), dataset, options);
            // setup overview
            $.plot($("#overviewResponseTimePercentilesOverTime"), dataset, prepareOverviewOptions(options));
        }
};

// Response Time Percentiles Over Time
function refreshResponseTimePercentilesOverTime(fixTimestamps) {
    var infos = responseTimePercentilesOverTimeInfos;
    prepareSeries(infos.data);
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, 28800000);
    }
    if(isGraph($("#flotResponseTimePercentilesOverTime"))) {
        infos.createGraph();
    }else {
        var choiceContainer = $("#choicesResponseTimePercentilesOverTime");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotResponseTimePercentilesOverTime", "#overviewResponseTimePercentilesOverTime");
        $('#footerResponseTimePercentilesOverTime .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};


var responseTimeVsRequestInfos = {
    data: {"result": {"minY": 141.0, "minX": 3.0, "maxY": 6333.0, "series": [{"data": [[8.0, 6333.0], [23.0, 3570.5]], "isOverall": false, "label": "Successes", "isController": false}, {"data": [[19.0, 160.0], [22.0, 565.0], [23.0, 141.0], [3.0, 482.0], [25.0, 198.0]], "isOverall": false, "label": "Failures", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 1000, "maxX": 25.0, "title": "Response Time Vs Request"}},
    getOptions: function() {
        return {
            series: {
                lines: {
                    show: false
                },
                points: {
                    show: true
                }
            },
            xaxis: {
                axisLabel: "Global number of requests per second",
                axisLabelUseCanvas: true,
                axisLabelFontSizePixels: 12,
                axisLabelFontFamily: 'Verdana, Arial',
                axisLabelPadding: 20,
            },
            yaxis: {
                axisLabel: "Median Response Time in ms",
                axisLabelUseCanvas: true,
                axisLabelFontSizePixels: 12,
                axisLabelFontFamily: 'Verdana, Arial',
                axisLabelPadding: 20,
            },
            legend: {
                noColumns: 2,
                show: true,
                container: '#legendResponseTimeVsRequest'
            },
            selection: {
                mode: 'xy'
            },
            grid: {
                hoverable: true // IMPORTANT! this is needed for tooltip to work
            },
            tooltip: true,
            tooltipOpts: {
                content: "%s : Median response time at %x req/s was %y ms"
            },
            colors: ["#9ACD32", "#FF6347"]
        };
    },
    createGraph: function () {
        var data = this.data;
        var dataset = prepareData(data.result.series, $("#choicesResponseTimeVsRequest"));
        var options = this.getOptions();
        prepareOptions(options, data);
        $.plot($("#flotResponseTimeVsRequest"), dataset, options);
        // setup overview
        $.plot($("#overviewResponseTimeVsRequest"), dataset, prepareOverviewOptions(options));

    }
};

// Response Time vs Request
function refreshResponseTimeVsRequest() {
    var infos = responseTimeVsRequestInfos;
    prepareSeries(infos.data);
    if (isGraph($("#flotResponseTimeVsRequest"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesResponseTimeVsRequest");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotResponseTimeVsRequest", "#overviewResponseTimeVsRequest");
        $('#footerResponseRimeVsRequest .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};


var latenciesVsRequestInfos = {
    data: {"result": {"minY": 141.0, "minX": 3.0, "maxY": 6332.0, "series": [{"data": [[8.0, 6332.0], [23.0, 3570.5]], "isOverall": false, "label": "Successes", "isController": false}, {"data": [[19.0, 160.0], [22.0, 565.0], [23.0, 141.0], [3.0, 475.0], [25.0, 198.0]], "isOverall": false, "label": "Failures", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 1000, "maxX": 25.0, "title": "Latencies Vs Request"}},
    getOptions: function() {
        return{
            series: {
                lines: {
                    show: false
                },
                points: {
                    show: true
                }
            },
            xaxis: {
                axisLabel: "Global number of requests per second",
                axisLabelUseCanvas: true,
                axisLabelFontSizePixels: 12,
                axisLabelFontFamily: 'Verdana, Arial',
                axisLabelPadding: 20,
            },
            yaxis: {
                axisLabel: "Median Latency in ms",
                axisLabelUseCanvas: true,
                axisLabelFontSizePixels: 12,
                axisLabelFontFamily: 'Verdana, Arial',
                axisLabelPadding: 20,
            },
            legend: { noColumns: 2,show: true, container: '#legendLatencyVsRequest' },
            selection: {
                mode: 'xy'
            },
            grid: {
                hoverable: true // IMPORTANT! this is needed for tooltip to work
            },
            tooltip: true,
            tooltipOpts: {
                content: "%s : Median Latency time at %x req/s was %y ms"
            },
            colors: ["#9ACD32", "#FF6347"]
        };
    },
    createGraph: function () {
        var data = this.data;
        var dataset = prepareData(data.result.series, $("#choicesLatencyVsRequest"));
        var options = this.getOptions();
        prepareOptions(options, data);
        $.plot($("#flotLatenciesVsRequest"), dataset, options);
        // setup overview
        $.plot($("#overviewLatenciesVsRequest"), dataset, prepareOverviewOptions(options));
    }
};

// Latencies vs Request
function refreshLatenciesVsRequest() {
        var infos = latenciesVsRequestInfos;
        prepareSeries(infos.data);
        if(isGraph($("#flotLatenciesVsRequest"))){
            infos.createGraph();
        }else{
            var choiceContainer = $("#choicesLatencyVsRequest");
            createLegend(choiceContainer, infos);
            infos.createGraph();
            setGraphZoomable("#flotLatenciesVsRequest", "#overviewLatenciesVsRequest");
            $('#footerLatenciesVsRequest .legendColorBox > div').each(function(i){
                $(this).clone().prependTo(choiceContainer.find("li").eq(i));
            });
        }
};

var hitsPerSecondInfos = {
        data: {"result": {"minY": 1.6666666666666667, "minX": 1.77406512E12, "maxY": 1.6666666666666667, "series": [{"data": [[1.77406512E12, 1.6666666666666667]], "isOverall": false, "label": "hitsPerSecond", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 60000, "maxX": 1.77406512E12, "title": "Hits Per Second"}},
        getOptions: function() {
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Number of hits / sec",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: "#legendHitsPerSecond"
                },
                selection: {
                    mode : 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s at %x was %y.2 hits/sec"
                }
            };
        },
        createGraph: function createGraph() {
            var data = this.data;
            var dataset = prepareData(data.result.series, $("#choicesHitsPerSecond"));
            var options = this.getOptions();
            prepareOptions(options, data);
            $.plot($("#flotHitsPerSecond"), dataset, options);
            // setup overview
            $.plot($("#overviewHitsPerSecond"), dataset, prepareOverviewOptions(options));
        }
};

// Hits per second
function refreshHitsPerSecond(fixTimestamps) {
    var infos = hitsPerSecondInfos;
    prepareSeries(infos.data);
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, 28800000);
    }
    if (isGraph($("#flotHitsPerSecond"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesHitsPerSecond");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotHitsPerSecond", "#overviewHitsPerSecond");
        $('#footerHitsPerSecond .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
}

var codesPerSecondInfos = {
        data: {"result": {"minY": 0.16666666666666666, "minX": 1.77406512E12, "maxY": 1.5, "series": [{"data": [[1.77406512E12, 0.16666666666666666]], "isOverall": false, "label": "200", "isController": false}, {"data": [[1.77406512E12, 1.5]], "isOverall": false, "label": "500", "isController": false}], "supportsControllersDiscrimination": false, "granularity": 60000, "maxX": 1.77406512E12, "title": "Codes Per Second"}},
        getOptions: function(){
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Number of responses / sec",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: "#legendCodesPerSecond"
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "Number of Response Codes %s at %x was %y.2 responses / sec"
                }
            };
        },
    createGraph: function() {
        var data = this.data;
        var dataset = prepareData(data.result.series, $("#choicesCodesPerSecond"));
        var options = this.getOptions();
        prepareOptions(options, data);
        $.plot($("#flotCodesPerSecond"), dataset, options);
        // setup overview
        $.plot($("#overviewCodesPerSecond"), dataset, prepareOverviewOptions(options));
    }
};

// Codes per second
function refreshCodesPerSecond(fixTimestamps) {
    var infos = codesPerSecondInfos;
    prepareSeries(infos.data);
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, 28800000);
    }
    if(isGraph($("#flotCodesPerSecond"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesCodesPerSecond");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotCodesPerSecond", "#overviewCodesPerSecond");
        $('#footerCodesPerSecond .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var transactionsPerSecondInfos = {
        data: {"result": {"minY": 0.16666666666666666, "minX": 1.77406512E12, "maxY": 1.5, "series": [{"data": [[1.77406512E12, 1.5]], "isOverall": false, "label": "参与秒杀-failure", "isController": false}, {"data": [[1.77406512E12, 0.16666666666666666]], "isOverall": false, "label": "参与秒杀-success", "isController": false}], "supportsControllersDiscrimination": true, "granularity": 60000, "maxX": 1.77406512E12, "title": "Transactions Per Second"}},
        getOptions: function(){
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Number of transactions / sec",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: "#legendTransactionsPerSecond"
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s at %x was %y transactions / sec"
                }
            };
        },
    createGraph: function () {
        var data = this.data;
        var dataset = prepareData(data.result.series, $("#choicesTransactionsPerSecond"));
        var options = this.getOptions();
        prepareOptions(options, data);
        $.plot($("#flotTransactionsPerSecond"), dataset, options);
        // setup overview
        $.plot($("#overviewTransactionsPerSecond"), dataset, prepareOverviewOptions(options));
    }
};

// Transactions per second
function refreshTransactionsPerSecond(fixTimestamps) {
    var infos = transactionsPerSecondInfos;
    prepareSeries(infos.data);
    if(infos.data.result.series.length == 0) {
        setEmptyGraph("#bodyTransactionsPerSecond");
        return;
    }
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, 28800000);
    }
    if(isGraph($("#flotTransactionsPerSecond"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesTransactionsPerSecond");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotTransactionsPerSecond", "#overviewTransactionsPerSecond");
        $('#footerTransactionsPerSecond .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

var totalTPSInfos = {
        data: {"result": {"minY": 0.16666666666666666, "minX": 1.77406512E12, "maxY": 1.5, "series": [{"data": [[1.77406512E12, 0.16666666666666666]], "isOverall": false, "label": "Transaction-success", "isController": false}, {"data": [[1.77406512E12, 1.5]], "isOverall": false, "label": "Transaction-failure", "isController": false}], "supportsControllersDiscrimination": true, "granularity": 60000, "maxX": 1.77406512E12, "title": "Total Transactions Per Second"}},
        getOptions: function(){
            return {
                series: {
                    lines: {
                        show: true
                    },
                    points: {
                        show: true
                    }
                },
                xaxis: {
                    mode: "time",
                    timeformat: getTimeFormat(this.data.result.granularity),
                    axisLabel: getElapsedTimeLabel(this.data.result.granularity),
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20,
                },
                yaxis: {
                    axisLabel: "Number of transactions / sec",
                    axisLabelUseCanvas: true,
                    axisLabelFontSizePixels: 12,
                    axisLabelFontFamily: 'Verdana, Arial',
                    axisLabelPadding: 20
                },
                legend: {
                    noColumns: 2,
                    show: true,
                    container: "#legendTotalTPS"
                },
                selection: {
                    mode: 'xy'
                },
                grid: {
                    hoverable: true // IMPORTANT! this is needed for tooltip to
                                    // work
                },
                tooltip: true,
                tooltipOpts: {
                    content: "%s at %x was %y transactions / sec"
                },
                colors: ["#9ACD32", "#FF6347"]
            };
        },
    createGraph: function () {
        var data = this.data;
        var dataset = prepareData(data.result.series, $("#choicesTotalTPS"));
        var options = this.getOptions();
        prepareOptions(options, data);
        $.plot($("#flotTotalTPS"), dataset, options);
        // setup overview
        $.plot($("#overviewTotalTPS"), dataset, prepareOverviewOptions(options));
    }
};

// Total Transactions per second
function refreshTotalTPS(fixTimestamps) {
    var infos = totalTPSInfos;
    // We want to ignore seriesFilter
    prepareSeries(infos.data, false, true);
    if(fixTimestamps) {
        fixTimeStamps(infos.data.result.series, 28800000);
    }
    if(isGraph($("#flotTotalTPS"))){
        infos.createGraph();
    }else{
        var choiceContainer = $("#choicesTotalTPS");
        createLegend(choiceContainer, infos);
        infos.createGraph();
        setGraphZoomable("#flotTotalTPS", "#overviewTotalTPS");
        $('#footerTotalTPS .legendColorBox > div').each(function(i){
            $(this).clone().prependTo(choiceContainer.find("li").eq(i));
        });
    }
};

// Collapse the graph matching the specified DOM element depending the collapsed
// status
function collapse(elem, collapsed){
    if(collapsed){
        $(elem).parent().find(".fa-chevron-up").removeClass("fa-chevron-up").addClass("fa-chevron-down");
    } else {
        $(elem).parent().find(".fa-chevron-down").removeClass("fa-chevron-down").addClass("fa-chevron-up");
        if (elem.id == "bodyBytesThroughputOverTime") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshBytesThroughputOverTime(true);
            }
            document.location.href="#bytesThroughputOverTime";
        } else if (elem.id == "bodyLatenciesOverTime") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshLatenciesOverTime(true);
            }
            document.location.href="#latenciesOverTime";
        } else if (elem.id == "bodyCustomGraph") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshCustomGraph(true);
            }
            document.location.href="#responseCustomGraph";
        } else if (elem.id == "bodyConnectTimeOverTime") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshConnectTimeOverTime(true);
            }
            document.location.href="#connectTimeOverTime";
        } else if (elem.id == "bodyResponseTimePercentilesOverTime") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshResponseTimePercentilesOverTime(true);
            }
            document.location.href="#responseTimePercentilesOverTime";
        } else if (elem.id == "bodyResponseTimeDistribution") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshResponseTimeDistribution();
            }
            document.location.href="#responseTimeDistribution" ;
        } else if (elem.id == "bodySyntheticResponseTimeDistribution") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshSyntheticResponseTimeDistribution();
            }
            document.location.href="#syntheticResponseTimeDistribution" ;
        } else if (elem.id == "bodyActiveThreadsOverTime") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshActiveThreadsOverTime(true);
            }
            document.location.href="#activeThreadsOverTime";
        } else if (elem.id == "bodyTimeVsThreads") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshTimeVsThreads();
            }
            document.location.href="#timeVsThreads" ;
        } else if (elem.id == "bodyCodesPerSecond") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshCodesPerSecond(true);
            }
            document.location.href="#codesPerSecond";
        } else if (elem.id == "bodyTransactionsPerSecond") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshTransactionsPerSecond(true);
            }
            document.location.href="#transactionsPerSecond";
        } else if (elem.id == "bodyTotalTPS") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshTotalTPS(true);
            }
            document.location.href="#totalTPS";
        } else if (elem.id == "bodyResponseTimeVsRequest") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshResponseTimeVsRequest();
            }
            document.location.href="#responseTimeVsRequest";
        } else if (elem.id == "bodyLatenciesVsRequest") {
            if (isGraph($(elem).find('.flot-chart-content')) == false) {
                refreshLatenciesVsRequest();
            }
            document.location.href="#latencyVsRequest";
        }
    }
}

/*
 * Activates or deactivates all series of the specified graph (represented by id parameter)
 * depending on checked argument.
 */
function toggleAll(id, checked){
    var placeholder = document.getElementById(id);

    var cases = $(placeholder).find(':checkbox');
    cases.prop('checked', checked);
    $(cases).parent().children().children().toggleClass("legend-disabled", !checked);

    var choiceContainer;
    if ( id == "choicesBytesThroughputOverTime"){
        choiceContainer = $("#choicesBytesThroughputOverTime");
        refreshBytesThroughputOverTime(false);
    } else if(id == "choicesResponseTimesOverTime"){
        choiceContainer = $("#choicesResponseTimesOverTime");
        refreshResponseTimeOverTime(false);
    }else if(id == "choicesResponseCustomGraph"){
        choiceContainer = $("#choicesResponseCustomGraph");
        refreshCustomGraph(false);
    } else if ( id == "choicesLatenciesOverTime"){
        choiceContainer = $("#choicesLatenciesOverTime");
        refreshLatenciesOverTime(false);
    } else if ( id == "choicesConnectTimeOverTime"){
        choiceContainer = $("#choicesConnectTimeOverTime");
        refreshConnectTimeOverTime(false);
    } else if ( id == "choicesResponseTimePercentilesOverTime"){
        choiceContainer = $("#choicesResponseTimePercentilesOverTime");
        refreshResponseTimePercentilesOverTime(false);
    } else if ( id == "choicesResponseTimePercentiles"){
        choiceContainer = $("#choicesResponseTimePercentiles");
        refreshResponseTimePercentiles();
    } else if(id == "choicesActiveThreadsOverTime"){
        choiceContainer = $("#choicesActiveThreadsOverTime");
        refreshActiveThreadsOverTime(false);
    } else if ( id == "choicesTimeVsThreads"){
        choiceContainer = $("#choicesTimeVsThreads");
        refreshTimeVsThreads();
    } else if ( id == "choicesSyntheticResponseTimeDistribution"){
        choiceContainer = $("#choicesSyntheticResponseTimeDistribution");
        refreshSyntheticResponseTimeDistribution();
    } else if ( id == "choicesResponseTimeDistribution"){
        choiceContainer = $("#choicesResponseTimeDistribution");
        refreshResponseTimeDistribution();
    } else if ( id == "choicesHitsPerSecond"){
        choiceContainer = $("#choicesHitsPerSecond");
        refreshHitsPerSecond(false);
    } else if(id == "choicesCodesPerSecond"){
        choiceContainer = $("#choicesCodesPerSecond");
        refreshCodesPerSecond(false);
    } else if ( id == "choicesTransactionsPerSecond"){
        choiceContainer = $("#choicesTransactionsPerSecond");
        refreshTransactionsPerSecond(false);
    } else if ( id == "choicesTotalTPS"){
        choiceContainer = $("#choicesTotalTPS");
        refreshTotalTPS(false);
    } else if ( id == "choicesResponseTimeVsRequest"){
        choiceContainer = $("#choicesResponseTimeVsRequest");
        refreshResponseTimeVsRequest();
    } else if ( id == "choicesLatencyVsRequest"){
        choiceContainer = $("#choicesLatencyVsRequest");
        refreshLatenciesVsRequest();
    }
    var color = checked ? "black" : "#818181";
    if(choiceContainer != null) {
        choiceContainer.find("label").each(function(){
            this.style.color = color;
        });
    }
}

