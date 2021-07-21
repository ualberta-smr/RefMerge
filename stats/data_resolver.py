import pandas as pd
from sqlalchemy import create_engine
import matplotlib.pyplot as plt
import numpy as np
import sys
import re
from scipy import stats
from scipy.stats import ranksums
import math


def get_db_connection():
    username = 'root'
    password = "password"
#    database_name = 'intelliMerge_data1'
    database_name = 'original_analysis'
    server = '127.0.0.1'

#    with open("../database.properties", 'r') as db_file:
#        for line in db_file:
#          line = line.strip()
#            username_search = re.search('^development.username=(.*)$', line, re.IGNORECASE)
#            password_search = re.search('^development.password=(.*)$', line, re.IGNORECASE)
#            url_search = re.search('^development.url=jdbc:mysql://(.*)/(.*)$', line, re.IGNORECASE)
#
#            if username_search:
#                username = username_search.group(1)
#            if password_search:
#                password = password_search.group(1)
#            if url_search:
#                server = url_search.group(1)
#                database_name = url_search.group(2)

    return create_engine('mysql+pymysql://{}:{}@{}/{}'.format(username, password, server, database_name))


def regions_intersect(region_1_start, region_1_length, region_2_start, region_2_length):
    if region_1_start + region_1_length < region_2_start:
        return False
    elif region_2_start + region_2_length < region_1_start:
        return False
    return True


def record_involved(x):
    is_source = (x['type'] == 's' and
                 x['old_path'] == x['path'] and
                 regions_intersect(x['old_start_line'], x['old_length'], x['start_line'], x['length']))
    is_dest = (x['type'] == 'd' and
               x['new_path'] == x['path'] and
               regions_intersect(x['new_start_line'], x['new_length'], x['start_line'], x['length']))
    if is_source or is_dest:
        project_url = get_project_url(x['merge_commit_id'])
        merge_commit_hash = get_merge_commit_hash(x['merge_commit_id'])
        write_to_csv(project_url, merge_commit_hash)
    return is_source or is_dest


accepted_types = ['Change Package', 'Extract And Move Method', 'Extract Interface', 'Extract Method',
                      'Extract Superclass', 'Inline Method', 'Move And Rename Class', 'Move Attribute', 'Move Class',
                      'Move Method', 'Pull Up Attribute', 'Pull Up Method', 'Pull Up Method', 'Push Down Method',
                      'Rename Class', 'Rename Method']

def write_to_projects_file(project_url):
    f = open('refMerge_evaluation_projects', 'r')
    lines = f.read()
    f.close()
    if project_url in lines:
        return
    with open('refMerge_evaluation_projects', 'a') as open_file:
        open_file.write(project_url + '\n')

def write_to_csv(project_url, merge_commit_hash):
    f = open('intelliMerge_data', 'r')
    lines = f.read()
    f.close()
    with open('intelliMerge_data', 'a') as open_file:
        line = []
        line.append(project_url)
        line.append(merge_commit_hash)
        entry = ";".join(line)
        found = False
    
        if merge_commit_hash in lines:
            return
        open_file.write(entry + '\n') 

def get_refactoring_types_sql_condition():
    type_condition = str()
    for ref_type in accepted_types:
        type_condition += 'refactoring_type = \"{}\" or '.format(ref_type)
    return type_condition[:-4]


def read_sql_table(table):
    print('Reading table {} from the database'.format(table))
    query = 'SELECT * FROM ' + table
    df = pd.read_sql(query, get_db_connection())
    return df

def get_project_by_id(project_id):
    query = "SELECT * FROM project WHERE id = '{}'".format(project_id)
    df = pd.read_sql(query, get_db_connection())
    return df.iloc[0]['url']

def get_project_url(merge_commit_id):
    query = "SELECT * FROM merge_commit WHERE id = '{}'".format(merge_commit_id)
    df = pd.read_sql(query, get_db_connection())
    project_id = df.iloc[0]['project_id']
    query = "SELECT * FROM project WHERE id ='{}'".format(project_id)
    df = pd.read_sql(query, get_db_connection())
    return df.iloc[0]['url']

def get_merge_commit_hash(merge_commit_id):
    query = "SELECT * FROM merge_commit WHERE id = '{}'".format(merge_commit_id)
    df = pd.read_sql(query, get_db_connection())
    return df.iloc[0]['commit_hash']


def get_projects():
    query = "SELECT * FROM project"
    df = pd.read_sql(query, get_db_connection())
    return df

def get_merge_commits():
    return read_sql_table('merge_commit')


def get_conflicting_regions():
    return read_sql_table('conflicting_region')


def get_conflicting_region_histories():
    return read_sql_table('conflicting_region_history')

def get_refactorings():
    return read_sql_table('refactoring')

def get_refactoring_regions():
    return read_sql_table('refactoring_region')


def get_accepted_refactoring_regions():
    print('Reading table refactoring_region from the database')

    query = 'select * from refactoring_region where refactoring_id in (select id from refactoring where ({}))'\
        .format(get_refactoring_types_sql_condition())
    return pd.read_sql(query, get_db_connection())



def get_conflicting_regions_by_involved_refactorings_per_merge_commit():
    conflicting_region_histories = get_conflicting_region_histories()
    refactoring_regions = get_accepted_refactoring_regions()

    for project_url in get_projects.get('url'):
        write_to_projects_file(project_url)

    cr_count_per_merge = conflicting_region_histories.groupby('merge_commit_id').conflicting_region_id.nunique().to_frame().rename(columns={'conflicting_region_id': 'cr_count'})

    involved_cr_count_per_merge = pd.DataFrame()
    rr_grouped_by_project = refactoring_regions.groupby('project_id')
    counter = 0
    for project_id, project_crh in conflicting_region_histories.groupby('project_id'):
        counter += 1
        print('Processing project {}'.format(counter))
        if project_id not in rr_grouped_by_project.groups:
            continue
        project_rrs = rr_grouped_by_project.get_group(project_id)
        crh_rr_combined = pd.merge(project_crh.reset_index(), project_rrs.reset_index(), on='commit_hash', how='inner')
        involved = crh_rr_combined[crh_rr_combined.apply(record_involved, axis=1)]
        involved_cr_count_per_merge = involved_cr_count_per_merge.append(involved.groupby('merge_commit_id').conflicting_region_id.nunique().to_frame().rename(columns={'conflicting_region_id': 'involved_cr_count'}))

    rq1_table = cr_count_per_merge.join(involved_cr_count_per_merge, how='outer').fillna(0).astype(int)
    rq1_table['percent'] = rq1_table['involved_cr_count'] / rq1_table['cr_count']
    return rq1_table

def get_refactorings_per_project():
    projects = get_projects()
    project_ids = projects.get('id')
    refactorings_per_project = []
    refactorings = get_refactorings()
    refactorings_grouped_by_project = refactorings.groupby('project_id')
    size = len(refactorings_grouped_by_project)
    refactorings_per_project = [0 for i in range(size)]
    projects_with_refactorings = [0 for i in range(size)]
    i = 0
    for project_id in project_ids:
        count = 0
        if project_id not in refactorings_grouped_by_project.groups:
            continue
        group = refactorings_grouped_by_project.get_group(project_id)
        count = group.count()['id']
        refactorings_per_project[i] = count
        projects_with_refactorings[i] = project_id
        i = i + 1

    fig, ax = plt.subplots()
    q = [0., 0.3, 0.7, 1.]
    bin_edges = stats.mstats.mquantiles(refactorings_per_project, q)
    N, bins, patches = plt.hist(refactorings_per_project, bins = bin_edges)    
    plt.title("Refactorings per project")
    ax.set_xlabel("Total Refactorings")
    ax.set_ylabel("Number of Projects")
    ax.set_xscale("log")
    patches[0].set_facecolor('green')
    patches[1].set_facecolor('red')
    patches[2].set_facecolor('blue')
    fig.tight_layout()
    plt.savefig('refactorings_per_project_histogram.pdf')

    np.random.seed(338219)
    # Get the projects in each bin
    for i in range(1, len(bin_edges)):
        e1 = bin_edges[i-1]
        e2 = bin_edges[i]
        values = np.where(np.logical_and(refactorings_per_project>=e1, refactorings_per_project<=e2))
        for j in range(round(10*(q[i] - q[i-1]))):
            qArray = values[0]
            val = round(np.random.random() * len(qArray))
            index = qArray[val]
            project_id = projects_with_refactorings[index]
            write_to_projects_file(get_project_by_id(project_id))
            

if __name__ == '__main__':
    f = open("intelliMerge_data", "w+")
    f.close()
#    get_conflicting_regions_by_involved_refactorings_per_merge_commit()
    get_refactorings_per_project()
