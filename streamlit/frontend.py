import pandas as pd
import streamlit as st
import requests
import xml.etree.ElementTree as ET
import altair as alt

# Function to handle API request
def search_api(query, resultnumber=10):
    # Example API endpoint (you'll need to replace this with the actual one)
    url = f"http://localhost:8080/searchLucene?searchstring={query}&resultnumber={resultnumber}"
    response = requests.get(url)

    if response.status_code == 200:
        # Parse the XML response
        root = ET.fromstring(response.content)
        return parse_xml(root)
    else:
        return {"Error": "Failed to fetch data"}

def search_api_monetdb(query, year=2024, resultnumber=10):
    # Example API endpoint (you'll need to replace this with the actual one)
    url = f"http://localhost:8080/searchMariaDB?searchstring={query}&resultnumber={resultnumber}&year={year}"
    response = requests.get(url)

    if response.status_code == 200:
        # Parse the XML response
        root = ET.fromstring(response.content)
        return parse_xml(root)
    else:
        return {"Error": "Failed to fetch data"}

def search_api_monetdb_meta(query, timestamp=2024, resultnumber=10):
    # Example API endpoint (you'll need to replace this with the actual one)
    url = f"http://localhost:8080/searchMariaDB-Metadata?searchstring={query}&resultnumber={resultnumber}&timestamp={timestamp}"
    response = requests.get(url)

    if response.status_code == 200:
        # Parse the XML response
        root = ET.fromstring(response.content)
        return parse_xml_meta(root)
    else:
        return {"Error": "Failed to fetch data"}

def get_queries():
    url = f"http://localhost:8080/queries"
    response = requests.get(url)
    if response.status_code == 200:
        root = ET.fromstring(response.content)
        return parse_queries(root)
    else:
        return {"Error": "Failed to fetch data"}

# Function to parse XML data into a dictionary (or any other format you prefer)
def parse_xml(xml_data):
    # Create a list of dictionaries to hold the extracted data
    data = []
    for item in xml_data.findall('item'):
        name = item.find('name').text
        score = item.find('score').text
        data.append({'Name': name, 'Score': float(score)})
    return data

def parse_xml_meta(xml_data):
    data = []


    # Extract relevant information
    query = xml_data.find('query').text
    result_count = int(xml_data.find('resultCount').text)
    result_hash = xml_data.find('resultHash').text

    # Extract nested results information
    results = []
    for result in xml_data.findall(".//results/results"):
        name = result.find('name').text
        score = float(result.find('score').text)
        results.append({'Name': name, 'Score': float(score)})

    data.append({'query': query, "result_count": result_count, "result_hash": result_hash, 'results': results})
    return data


def parse_queries(xml_data):
    data = []
    for item in xml_data.findall('item'):
        id = item.find('id').text
        query = item.find('query').text
        result_count = item.find('result_count').text
        executed = item.find('executed').text
        result_hash = item.find('result_hash').text
        data.append({'id': id, "query": query, 'result_count': result_count, 'executed': executed, "result_hash": result_hash})
    return data


st.set_page_config(layout="wide")
# Sidebar for navigation
#st.sidebar.title("Navigation")
page = st.sidebar.radio("Go to", ["General Information", "Time Travel", "Reproducibility", "Change over Time", "Score"])



# Common header for all pages
st.title("TimIR - Time Traveling through IR History")

# Navigation buttons under the title
#page = st.radio("", ["Time Travel", "Reproducibility", "Change over Time", "Score"], horizontal=True)



if page == "General Information":
    st.header("Motivation")
    st.write("In some settings, the reproducibility of ranked lists is desirable, such as when extracting a subset of an evolving document corpus for research or in domains such as patent retrieval, in medical systematic reviews or when exploring the rich history of Information Retrieval. Currently the only reliable way of achieving reproducibility in Information Retrieval Settings is Boolean Retrieval, which has therefore became the standard retrieval strategy for such tasks. ")
    st.write("We showcase a hybrid retrieval strategy which combines a fast traditional sparse retrieval engine (Lucene) for live queries and a slower columnstore retrieval engine (MonetDB) that keeps all historical changes to the term statistics and is able to recreate the document statistics for a given point in time.")
    st.write("To showcase our proposed system in a small real-world example, we indexed all abstracts of the IR Anthology until 2021 in yearly increments, this includes 52780 documents, 63882 terms in the dictionary, and 2701478 total used terms.")
    st.header("Indexing Strategy")
    st.write("For indexing, we built upon the functionality by Lucene, and are indexing the IR Anthology by Lucene. Once it is processed, the term and document statistics are then mirrored and versioned in MonetDB. Therefore, we applied the RDA Dynamic Data Citation guidelines to our database, and extended each document in the corpora with a validity period. The validity period shows, when a document was added to the index and at which point it was deleted from the corpora. With this information it is then possible to calculate the relevant frequencies for a given point in time.")
    st.header("Functionality")
    st.write("Our System supports a wide range of different tasks, as analyzing the performance of search queries over time, by travelling through the historical states of the index, recreating the index for a given point in time, and last but not least the citation of queries with variable subsets of data. To showcase that queries are reproducible, we further store at the time of execution a hash of the result list to allow the verification of the recreation of the ranked list.")

# Page-specific content
if page == "Time Travel":
    # Common search bar
    search_triggered = False
    query = st.text_input("Enter your search query:")
    # Year slider (you can customize the range)
    year = st.slider("Select Year", min_value=1970, max_value=2021, value=2021)
    if query:
        result = pd.DataFrame(search_api_monetdb(query, year ))
        result['year'] = year
        result['Position'] = result.index + 1
        df_display = result[['Position', 'Name', 'Score']].reset_index(drop=True)
        _, col_center, _ = st.columns([1,2,1])
        with col_center:
            st.dataframe(df_display, hide_index=True, use_container_width =True)

elif page == "Reproducibility":
    # Common search bar
    queries = get_queries()
    df = pd.DataFrame(queries)
    st.dataframe(df, hide_index=True)

    selected_query = st.selectbox("Choose a query to re-execute:", df['query'])

    if selected_query:
        selected_row = df[df['query'] == selected_query].iloc[0]
        result = search_api_monetdb_meta(selected_row['query'], selected_row['executed'], selected_row['result_count'])
        col1, col2 = st.columns(2)
        with col1:
            st.write(f"Query executed: {result[0].get('query')}")
            st.write(f"Results obtained: {result[0].get('result_count')}")
            st.write(f"Result hash: {result[0].get('result_hash')}")
        with col2:
            st.dataframe(result[0].get("results"))

elif page == "Score":
    # Common search bar
    query = st.text_input("Enter your search query:")

    if query:
        result = search_api(query)
        result_monet = search_api_monetdb(query)
        df = pd.DataFrame(result)
        df_monet = pd.DataFrame(result)

        col1, col2 = st.columns([1,1])
        with col1:
            st.write("Lucene")
            st.dataframe(df)
        with col2:
            st.write("MonetDB")
            st.dataframe(df_monet)


elif page == "Change over Time":
    col1, col2, col3 = st.columns([2, 1, 1])
    search_triggered = False
    with col1:
        # Common search bar
        query = st.text_input("Enter your search query:")
    with col2:
        results = st.number_input("#Results", value=20)
    with col3:
        previous_years = st.number_input("#Years", value=5)

    year = st.slider("Select Year", min_value=1970, max_value=2021, value=2021)



    if year or query or previous_years or results:
        if query:
            df_list = []
            for i in range(0, previous_years):
                result = pd.DataFrame(search_api_monetdb(query, year - i, resultnumber=results))
                result['year'] = year - i
                result['position'] = result.index + 1
                df_list.append(result)
            df_total = pd.concat(df_list, ignore_index=True)

            col_chart1, col_chart2 = st.columns([1, 1])
            with col_chart1:
                # Create the line chart
                line = alt.Chart(df_total).mark_line().encode(
                    x='year:O',  # Or x='year:T' if year is a datetime
                    y=alt.Y('position:Q', scale=alt.Scale(reverse=True), axis=alt.Axis(values=list(range(1, results)))),
                    color=alt.Color('Name:N', legend=None),
                    tooltip=['Name', 'year', 'position', 'Score']
                ).properties(
                    title='Position Change Over Years'
                )

                # Create the point plot
                points = alt.Chart(df_total).mark_point().encode(
                    x='year:O',
                    y=alt.Y('position:Q', scale=alt.Scale(reverse=True)),
                    color=alt.Color('Name:N', legend=None),
                    tooltip=['Name', 'year', 'position', 'Score']
                )

                # Layer the line chart and point plot together
                position_chart = alt.layer(line, points).interactive()

                # Display chart in Streamlit
                st.altair_chart(position_chart, use_container_width=True)


            with col_chart2:
                # Define the min and max for the y-axis domain
                min_score = round(df_total['Score'].min() - 0.5)
                max_score = round(df_total['Score'].max())

                # Create the line chart
                line = alt.Chart(df_total).mark_line().encode(
                    x='year:O',  # Or x='year:T' if year is a datetime
                    y=alt.Y('Score:Q', scale=alt.Scale(domain=[min_score, max_score])),
                    color=alt.Color('Name:N', legend=None),
                    tooltip=['Name', 'year', 'Score']
                ).properties(
                    title='Score Change Over Years'
                )

                # Create the point plot
                points = alt.Chart(df_total).mark_point().encode(
                    x='year:O',
                    y=alt.Y('Score:Q', scale=alt.Scale(domain=[min_score, max_score])),
                    color=alt.Color('Name:N', legend=None),
                    tooltip=['Name', 'year', 'Score']
                )

                # Layer the line chart and point plot together
                score_chart = alt.layer(line, points).interactive()

                # Display chart in Streamlit
                st.altair_chart(score_chart, use_container_width=True)