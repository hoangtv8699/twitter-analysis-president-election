# Import Libraries
import pandas as pd
import numpy as np
import seaborn as sns
import matplotlib.pyplot as plt
from textblob import TextBlob
from wordcloud import WordCloud
import plotly.graph_objects as go
import plotly.express as px

import tweet_analysis

# Reading both the csv files
Trump_reviews = pd.read_csv('Trumpall2.csv', encoding='utf-8')
Trump_reviews = tweet_analysis.sentiment_analysis(Trump_reviews)

print(Trump_reviews.head())

new1 = Trump_reviews.groupby('analysis').count()
x = list(new1['polarity'])
y = list(new1.index)
tuple_list = list(zip(x,y))

df = pd.DataFrame(tuple_list, columns=['x','y'])

df['color'] = 'blue'
df['color'][1] = 'red'
df['color'][2] = 'green'

import plotly.graph_objects as go
fig = go.Figure(go.Bar(x=df['x'],
                y=df['y'],
                orientation ='h',
                marker={'color': df['color']}))
fig.show()
