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
    database_name = 'original_analysis'
    server = '127.0.0.1'

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
    #if is_source or is_dest:
    #    project_url = get_project_url(x['merge_commit_id'])
    #    merge_commit_hash = get_merge_commit_hash(x['merge_commit_id'])
        #write_to_csv(project_url, merge_commit_hash)
    return is_source or is_dest


accepted_types = ['Change Package', 'Extract And Move Method', 'Extract Interface', 'Extract Method',
                      'Extract Superclass', 'Inline Method', 'Move And Rename Class', 'Move Attribute', 'Move Class',
                      'Move Method', 'Pull Up Attribute', 'Pull Up Method', 'Pull Up Method', 'Push Down Method',
                      'Rename Class', 'Rename Method']

covered_types = ['Extract Method', 'Inline Method', 'Rename Method', 'Rename Class', 'Move Method', 'Move Class', 'Move And Rename Method', 'Move And Rename Class']



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

def get_project_id_by_url(project_url):
    query = "SELECT * FROM project WHERE url = '{}'".format(project_url)
    df = pd.read_sql(query, get_db_connection())
    return df.iloc[0]['id']


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

def get_merge_commits(project_id):
    query = "SELECT * FROM merge_commit WHERE project_id = '{}'".format(project_id)
    df = pd.read_sql(query, get_db_connection())
    return df


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


def get_total_merge_commits(project_id):
    merge_commits = get_merge_commits(project_id)
    return merge_commits.count()['id']

def get_total_conflicting_commits(project_id):
    merge_commits = get_merge_commits(project_id)
    group_by_conflicting = merge_commits.groupby('is_conflicting')
    conflicting_group = group_by_conflicting.get_group(1)
    return conflicting_group.count()['id']

def graph_refactorings():
    refactorings = get_refactorings()
    r_grouped_by_type = refactorings.groupby('refactoring_type')
    d = {}
    for r in r_grouped_by_type.groups:
        group = r_grouped_by_type.get_group(r)
        count = group.count()['refactoring_type']
        d[r] = count

    sorted_d = dict(sorted(d.items(), key=lambda item: item[1], reverse=True)[:10])
    colors = []

    for k in sorted_d:
        if k in covered_types:
            colors.append('black')
        else:
            colors.append('grey')

    fig, ax = plt.subplots()
    plt.bar(sorted_d.keys(), sorted_d.values(), color=colors)
    plt.title("Refactorings in Conflicting Regions")
    ax.set_xlabel("Refactoring Type")
    ax.set_ylabel("Total Refactorings")
    fig.autofmt_xdate()
    fig.tight_layout()
    plt.savefig('Refactorings in Conflicting Regions.pdf')



def get_involved_refactorings_per_project():
    f = open('refMerge_evaluation_projects', 'r')
    lines = f.read()
    f.close()

    projects = get_projects()
    project_ids = projects.get('id')
    ir_project = pd.DataFrame(columns={'project', 'involved_refs'})

    conflicting_regoins = get_conflicting_regions()
    conflicting_region_histories = get_conflicting_region_histories()
    refactoring_regions = get_accepted_refactoring_regions()

    rr_grouped_by_project = refactoring_regions.groupby('project_id')
    counter = 0
    for project_id, project_crh in conflicting_region_histories.groupby('project_id'):
        counter += 1
        print('Processing project {}'.format(counter))
        if project_id not in rr_grouped_by_project.groups:
            continue

        if get_project_by_id(project_id) in lines:
            continue

        project_rrs = rr_grouped_by_project.get_group(project_id)
        crh_rr_combined = pd.merge(project_crh.reset_index(), project_rrs.reset_index(), on='commit_hash', how='inner')
        involved = crh_rr_combined[crh_rr_combined.apply(record_involved, axis=1)]
        total_involved = len(involved.groupby('merge_commit_id').groups)
        if total_involved < 8:
            continue

        ir_project = ir_project.append({'project': project_id, 'involved_refs': total_involved}, ignore_index=True)

    return ir_project


def analyze_data():
    ir_project = get_data_frame('involved_refactorings_per_project')
    refs_per_proj = ir_project['involved_refs'].tolist()

    for _, x in ir_project.iterrows():
        print(x)

    fig, ax = plt.subplots()
    q = [0., 0.3, 0.7, 1.]
    bin_edges = stats.mstats.mquantiles(refs_per_proj, q)
    N, bins, patches = plt.hist(refs_per_proj, bins = bin_edges)
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
        logic = ir_project[(ir_project['involved_refs'] >= e1) & (ir_project['involved_refs']<=e2)]['project']
        values = np.where(np.logical_and(ir_project['involved_refs']>=e1, ir_project['involved_refs']<=e2))
        values = logic.tolist()
        for j in range(round(10*(q[i] - q[i-1]))):
            print(q[i], q[i-1])
            qArray = values
            val = round(np.random.random() * len(qArray))
            project_id = qArray[val]
            print(project_id, ": ", ir_project[ir_project['project'] == project_id]['involved_refs'].iloc[0])
            write_to_projects_file(get_project_by_id(project_id))


def get_data_frame(df_name):
    try:
        return pd.read_pickle(df_name + '.pickle')
    except FileNotFoundError:
        df = getattr(sys.modules[__name__], 'get_' + df_name)()
        df.to_pickle(df_name + '.pickle')
        return df


if __name__ == '__main__':
    get_data_frame('involved_refactorings_per_project')
    analyze_data()
