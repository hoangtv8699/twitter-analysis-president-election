import tags from "./tags.js";

am4core.ready(function () {
  // Themes begin
  am4core.useTheme(am4themes_animated);
  // Themes end

  var chart = am4core.create("chartdiv", am4charts.XYChart);
  chart.padding(10, 10, 10, 10);

  chart.numberFormatter.bigNumberPrefixes = [
    { number: 1e3, suffix: "K" },
    { number: 1e6, suffix: "M" },
    { number: 1e9, suffix: "B" },
  ];

  // chart.legend = new am4charts.Legend();
  // chart.legend.position = "right";


  var label = chart.plotContainer.createChild(am4core.Label);
  label.x = am4core.percent(97);
  label.y = am4core.percent(95);
  label.horizontalCenter = "right";
  label.verticalCenter = "middle";
  label.dx = -15;
  label.fontSize = 40;

  var playButton = chart.plotContainer.createChild(am4core.PlayButton);
  playButton.x = am4core.percent(97);
  playButton.y = am4core.percent(95);
  playButton.dy = -2;
  playButton.verticalCenter = "middle";
  playButton.events.on("toggled", function (event) {
    if (event.target.isActive) {
      play();
    } else {
      stop();
    }
  });

  chart.legend = new am4charts.Legend();
  chart.legend.position = "right";

  var stepDuration = 4000;

  var categoryAxis = chart.yAxes.push(new am4charts.CategoryAxis());
  categoryAxis.renderer.grid.template.location = 0;
  categoryAxis.dataFields.category = "choice";
  // categoryAxis.title.text = "Tags";
  categoryAxis.renderer.minGridDistance = 20;
  categoryAxis.renderer.grid.template.location = 0;
  categoryAxis.renderer.inversed = true;
  categoryAxis.renderer.grid.template.disabled = true;
  categoryAxis.renderer.cellStartLocation = 0.1;
  categoryAxis.renderer.cellEndLocation = 0.9;

  var valueAxis = chart.xAxes.push(new am4charts.ValueAxis());
  valueAxis.min = 0;
  valueAxis.rangeChangeEasing = am4core.ease.linear;
  valueAxis.rangeChangeDuration = stepDuration;
  valueAxis.extraMax = 0.1;
  valueAxis.title.text = "Number of tweets";

  var series = chart.series.push(new am4charts.ColumnSeries());
  series.dataFields.categoryY = "choice";
  series.dataFields.valueX = "numberOfTweets";
  series.name = "positive";
  series.columns.template.strokeOpacity = 0;
  series.interpolationDuration = stepDuration;
  series.interpolationEasing = am4core.ease.linear;
  series.columns.template.column.cornerRadiusBottomRight = 5;
  series.columns.template.column.cornerRadiusTopRight = 5;

  var series2 = chart.series.push(new am4charts.ColumnSeries());
  series2.dataFields.categoryY = "choice";
  series2.dataFields.valueX = "numberOfTweets2";
  series2.name = "negative";
  series2.columns.template.strokeOpacity = 0;
  series2.interpolationDuration = stepDuration;
  series2.interpolationEasing = am4core.ease.linear;
  series2.columns.template.column.cornerRadiusBottomRight = 5;
  series2.columns.template.column.cornerRadiusTopRight = 5;

  var series3 = chart.series.push(new am4charts.ColumnSeries());
  series3.dataFields.categoryY = "choice";
  series3.dataFields.valueX = "numberOfTweets3";
  series3.name = "neutral";
  series3.columns.template.strokeOpacity = 0;
  series3.interpolationDuration = stepDuration;
  series3.interpolationEasing = am4core.ease.linear;
  series3.columns.template.column.cornerRadiusBottomRight = 5;
  series3.columns.template.column.cornerRadiusTopRight = 5;

  var labelBullet = series.bullets.push(new am4charts.LabelBullet());
  labelBullet.locationX = 0.5;
  labelBullet.label.text = "{valueX}";
  labelBullet.label.fill = am4core.color("#fff"); 

  var labelBullet = series2.bullets.push(new am4charts.LabelBullet());
  labelBullet.locationX = 0.5;
  labelBullet.label.text = "{valueX}";
  labelBullet.label.fill = am4core.color("#fff");

  var labelBullet = series3.bullets.push(new am4charts.LabelBullet());
  labelBullet.locationX = 0.5;
  labelBullet.label.text = "{valueX}";
  labelBullet.label.fill = am4core.color("#fff");

  chart.zoomOutButton.disabled = true;

  var day = 3;
  var month = 11;
  label.text = day.toString() + "/" + month.toString();

  var interval;

  function play() {
    interval = setInterval(function () {
      nextDay();
    }, stepDuration);
    nextDay();
  }

  function stop() {
    if (interval) {
      clearInterval(interval);
    }
  }

  function nextDay() {
    day++;

    if (day > 30) {
      month++;
      day = 1;
    }

    if (day === 14 && month === 12) {
      day = 3;
      month = 11;
    }

    var newData = allData[day.toString() + "/" + month.toString()];

    var itemsWithNonZero = 0;
    var itemsWithNonZero2 = 0;
    var itemsWithNonZero3 = 0;
    for (var i = 0; i < chart.data.length; i++) {
      chart.data[i].numberOfTweets = newData[i].numberOfTweets;
      chart.data[i].numberOfTweets2 = newData[i].numberOfTweets2;
      chart.data[i].numberOfTweets3 = newData[i].numberOfTweets3;
      if (chart.data[i].numberOfTweets > 0) {
        itemsWithNonZero++;
      } 
      if (chart.data[i].numberOfTweets2 > 0) {
        itemsWithNonZero2++;
      }
      if (chart.data[i].numberOfTweets3 > 0) {
        itemsWithNonZero3++;
      }
    }

    if (month === 11 || month === 12) {
      series.interpolationDuration = stepDuration / 4;
      series2.interpolationDuration = stepDuration / 4;
      series3.interpolationDuration = stepDuration / 4;
      valueAxis.rangeChangeDuration = stepDuration / 4;
    } else {
      series.interpolationDuration = stepDuration;
      series2.interpolationDuration = stepDuration;
      series3.interpolationDuration = stepDuration;
      valueAxis.rangeChangeDuration = stepDuration;
    }

    chart.invalidateRawData();
    label.text = day.toString() + "/" + month.toString();

    categoryAxis.zoom({
      start: 0,
      end: itemsWithNonZero / categoryAxis.dataItems.length,
    });
  }

  categoryAxis.sortBySeries = series;
  categoryAxis.sortBySeries = series2;
  categoryAxis.sortBySeries = series3;

  function generateData() {
    var data = [];

    // generate random data for allData
    for (var i = 0; i < tags.length; i++) {
      var tweetNumber = Math.floor(Math.random() * 10000);
      var dataElement = {
        choice: tags[i],
        numberOfTweets: tweetNumber + Math.floor(Math.random() * 10000),
        numberOfTweets2: tweetNumber + Math.floor(Math.random() * 10000),
        numberOfTweets3: tweetNumber + Math.floor(Math.random() * 10000),
      };

      data.push(dataElement);
    }

    return data;
  }

  var numberOfDays = 30 - 3 + 14;
  var allData = [];
  var oldData;

  function fillData() {
    var dayLabel = 3;
    var monthLabel = 11;
    for (var i = 0; i < numberOfDays; i++) {
    
      var labelTag = dayLabel.toString() + "/" + monthLabel.toString();

      dayLabel++;

      if (dayLabel === 31 && monthLabel === 11) {
        dayLabel = 1;
        monthLabel = 12;
      }

      var dataArr = generateData();
      // add old data to new data
      if(oldData) { 
        for(var j = 0; j < tags.length; j++) {
          dataArr[j].numberOfTweets += oldData[j].numberOfTweets;
          dataArr[j].numberOfTweets2 += oldData[j].numberOfTweets2;
          dataArr[j].numberOfTweets3 += oldData[j].numberOfTweets3;
        }
      }
      
      oldData = dataArr;
      dataArr = dataArr.sort().slice(0, 5);

      // allData is main data array use for fill data
      allData[labelTag] = dataArr;
    }
  }

  fillData();

  chart.data = JSON.parse(
    JSON.stringify(allData[day.toString() + "/" + month.toString()])
  );
  categoryAxis.zoom({ start: 0, end: 1 / chart.data.length });

  series.events.on("inited", function () {
    setTimeout(function () {
      playButton.isActive = true; // this starts interval
    }, 4000);
  });


}); // end am4core.ready()
