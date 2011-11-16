$(function () {
      var updateInterval = 100;

      function hostUpdater () {
          var totalPoints = 50;

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

          function host_update() {
              function host_onDataReceived(series) {
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
                         success: host_onDataReceived,
                     });
              setTimeout(host_update, updateInterval);
          }

          host_update();
      }

      function prgUpdater () {
          var prgoptions = {
              series: {
                  pie: {
                      show: true
                  }
              }
          };

          function prg_update() {
              function prg_onDataReceived(prgseries) {
                  var prgdata = [];
                  var total = 0;
                  
                  $.each(prgseries, function(index,value) {
                             total += value;
                         });

                  $.each(prgseries, function(index,value) {
                             prgdata.push({label: index, data: value / total * 100.0});
                         });


                  console.log(prgdata);
                  $.plot($("#prgs"), prgdata, prgoptions);
              }

              $.ajax({
                         url: "/program.json",
                         method: 'GET',
                         dataType: 'json',
                         success: prg_onDataReceived,
                     });
              setTimeout(prg_update, updateInterval);
          }
          prg_update();
      }

      hostUpdater();
  });
