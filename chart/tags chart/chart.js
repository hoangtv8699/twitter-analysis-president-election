import tags from "./tags.js";

am4core.ready(function () {
  // Themes begin
  am4core.useTheme(am4themes_animated);
  // Themes end

  var chart = am4core.create("chartdiv", am4charts.XYChart);
  chart.padding(5, 5, 5, 5);

  chart.numberFormatter.bigNumberPrefixes = [
    { number: 1e3, suffix: "K" },
    { number: 1e6, suffix: "M" },
    { number: 1e9, suffix: "B" },
  ];

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

  var stepDuration = 4000;

  var categoryAxis = chart.yAxes.push(new am4charts.CategoryAxis());
  categoryAxis.renderer.grid.template.location = 0;
  categoryAxis.dataFields.category = "choice";
  categoryAxis.renderer.minGridDistance = 1;
  categoryAxis.renderer.inversed = true;
  categoryAxis.renderer.grid.template.disabled = true;

  var valueAxis = chart.xAxes.push(new am4charts.ValueAxis());
  valueAxis.min = 0;
  valueAxis.rangeChangeEasing = am4core.ease.linear;
  valueAxis.rangeChangeDuration = stepDuration;
  valueAxis.extraMax = 0.1;

  var series = chart.series.push(new am4charts.ColumnSeries());
  series.dataFields.categoryY = "choice";
  series.dataFields.valueX = "numberOfTweets";
  series.tooltipText = "{valueX.value}";
  series.columns.template.strokeOpacity = 0;
  series.columns.template.column.cornerRadiusBottomRight = 5;
  series.columns.template.column.cornerRadiusTopRight = 5;
  series.interpolationDuration = stepDuration;
  series.interpolationEasing = am4core.ease.linear;

  var labelBullet = series.bullets.push(new am4charts.LabelBullet());
  labelBullet.label.horizontalCenter = "right";
  labelBullet.label.text = "{values.valueX.workingValue.formatNumber('#.0as')}";
  labelBullet.label.textAlign = "end";
  labelBullet.label.dx = -10;

  chart.zoomOutButton.disabled = true;

  // as by default columns of the same series are of the same color, we add adapter which takes colors from chart.colors color set
  series.columns.template.adapter.add("fill", function (fill, target) {
    return chart.colors.getIndex(target.dataItem.index + 80);
  });

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
    for (var i = 0; i < chart.data.length; i++) {
      chart.data[i].numberOfTweets = newData[i].numberOfTweets;
      if (chart.data[i].numberOfTweets > 0) {
        itemsWithNonZero++;
      }
    }

    if (month === 11 || month === 12) {
      series.interpolationDuration = stepDuration / 4;
      valueAxis.rangeChangeDuration = stepDuration / 4;
    } else {
      series.interpolationDuration = stepDuration;
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

  function generateData() {
    var data = [];
    var tempTagsArr = tags;

    for (var i = 0; i < tags.length; i++) {
      var tweetNumber = Math.floor(Math.random() * 1000000);
      var tagIndex = Math.floor(Math.random() * tags.length);
      var dataElement = {
        choice: tempTagsArr[tagIndex],
        numberOfTweets: tweetNumber,
      };

      data.push(dataElement);
    }

    data = data.slice(0, 10);

    return data;
  }

  var numberOfDays = 30 - 3 + 1 + 14;
  var allData = [];

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

      allData[labelTag] = dataArr;
    }
  }

  fillData();

  console.log(allData);

  chart.data = JSON.parse(
    JSON.stringify(allData[day.toString() + "/" + month.toString()])
  );
  categoryAxis.zoom({ start: 0, end: 1 / chart.data.length });

  series.events.on("inited", function () {
    setTimeout(function () {
      playButton.isActive = true; // this starts interval
    }, 2000);
  });
}); // end am4core.ready()
