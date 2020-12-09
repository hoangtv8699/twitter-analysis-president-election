am4core.ready(function () {
  // Themes begin
  am4core.useTheme(am4themes_animated);
  // Themes end

  var chart = am4core.create("chartdiv", am4charts.XYChart);
  chart.padding(40, 40, 40, 40);

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
  label.fontSize = 50;

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
  series.dataFields.valueX = "percentage";
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
    return chart.colors.getIndex(target.dataItem.index);
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
      chart.data[i].percentage = newData[i].percentage;
      if (chart.data[i].percentage > 0) {
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

  var numberOfDays = 30 - 3 + 1 + 14;
  var allData = [];

  function fillData() {
    var trumpSide, joeSide, neutral;
    var dayLabel = 3;
    var monthLabel = 11;
    for (var i = 0; i < numberOfDays; i++) {
      var labelTag = dayLabel.toString() + "/" + monthLabel.toString();

      dayLabel++;

      if (dayLabel === 31 && monthLabel === 11) {
        dayLabel = 1;
        monthLabel = 12;
      }

      neutral = Math.floor(Math.random() * 91);
      trumpSide = parseInt((100 - neutral) / 3);
      joeSide = 100 - neutral - trumpSide;

      allData[labelTag] = [
        {
          choice: "Donald Trump",
          percentage: trumpSide,
        },
        { choice: "Joe Biden", percentage: joeSide },
        {
          choice: "Neutral/Not vote",
          percentage: neutral,
        },
      ];
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
    }, 2000);
  });
}); // end am4core.ready()
