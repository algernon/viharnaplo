$(function () {
      function hostUpdater () {
          var updateInterval = 100, totalPoints = 50;

          var options = {
              xaxis: {
                  mode: "time",
                  timeformat: "%y-%0m-%0d %H:%M:%S",
                  ticks: 100
              },
              grid: {
                  hoverable: true
              },
              series: {
                  shadowSize: 0,
                  stack: null,
                  lines: {
                      show: true,
                      fill: true,
                  },
                  points: {
                      show: true
                  }
              },
              legend: {
                  container: "#hostlegend",
              },
          };
          var data = {all: []};
          var host_total = {
              all: 0
          };

          function update() {
              function onDataReceived(series) {
                  var hc = {all: 0};
                  var received = {
                      all: 0
                  };
                  var pdata = [];

                  $.each(series, function(index,value) {
                             hc["all"] += parseInt(value, 10);
                             if (!hc[index])
                                 hc[index] = 0;
                             hc[index] += parseInt(value, 10);
                         });

                  $.each(hc, function(index, value) {
                             var d = new Date();

                             if (!host_total[index])
                                 host_total[index] = hc[index];
                             if (!data[index])
                                 data[index] = [];

                             var diff = hc[index] - host_total[index];

                             data[index].push ([d, diff]);
                             host_total[index] += diff;
                         });

                  if (data.all.length > totalPoints - 1) {
                      $.each (data, function(index, value) {
                                  data[index] = data[index].slice(1);
                              });
                  }
                  $.each (data, function (index, value) {
                              pdata.push({label: index, data: value});
                          });
                  
                  $.plot($("#hostmsgsec"), pdata, options);
              }

              $.ajax({
                         url: "/host.json",
                         method: 'GET',
                         dataType: 'json',
                         success: onDataReceived,
                     });
              setTimeout(update, updateInterval);
          }

          update();
      }

      hostUpdater();
  });
