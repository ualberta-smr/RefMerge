import pandas as pd
from sqlalchemy import create_engine
import matplotlib.pyplot as plt
import numpy as np
import sys
import re
from scipy import stats
from scipy.stats import ranksums
import math


d3 = {}
d4 = {}


def get_db_connection():
    username = 'root'
    password = "password"
    database_name = 'refMerge_dataset'
    server = '127.0.0.1'

    return create_engine('mysql+pymysql://{}:{}@{}/{}'.format(username, password, server, database_name))


covered_types = ['Extract Method', 'Inline Method', 'Rename Method', 'Rename Class', 'Move Method', 'Move Class', 'Move And Rename Method', 'Move And Rename Class']


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
        merge_commit = get_merge_commit(x['merge_commit_id'])
        line = []
        line.append(str(project_url))
        line.append(str(merge_commit['commit_hash']))
        line.append(str(merge_commit['parent_1']))
        line.append(str(merge_commit['parent_2']))
        line.append(str(merge_commit['timestamp']))
        entry = ";".join(line)
        total = write_to_evaluation_csv(entry)
        c_id = x['conflicting_region_id']
        if c_id in d3:
            d3[c_id] += total
            d4[c_id].append(get_refactoring_by_id(x['refactoring_id']))
        else:
            d3[c_id] = total
            d4[c_id] = [get_refactoring_by_id(x['refactoring_id'])]

    return is_source or is_dest


accepted_types = ['Change Package', 'Extract And Move Method', 'Extract Interface', 'Extract Method',
                      'Extract Superclass', 'Inline Method', 'Move And Rename Class', 'Move Attribute', 'Move Class',
                      'Move Method', 'Pull Up Attribute', 'Pull Up Method', 'Pull Up Method', 'Push Down Method',
                      'Rename Class', 'Rename Method']

supported_types = ['Rename Class', 'Rename Method', 'Move Class', 'Move Method', 'Move And Rename Class', 'Move And Rename Method', 'Extract Method', 'Inline Method']

unsupported_types = ['Change Package', 'Extract And Move Method', 'Move Field', 'Rename Parameter', 'Pull Up Field', 'Pull Up Method', 'Push Down Method', 'Push Down Field', 'Add Parameter', 'Change Parameter Type', 'Rename Package', 'Move Source Folder']



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

def write_to_evaluation_csv(entry):
    f = open('refMerge_evaluation_commits', 'r')
    lines = f.read()
    f.close()
    with open('refMerge_evaluation_commits', 'a') as f:
        if entry in lines:
            return 1
        f.write(entry + '\n')

    return 0



def get_refactoring_types_sql_condition():
    type_condition = str()
    for ref_type in supported_types:
        type_condition += 'refactoring_type = \"{}\" or '.format(ref_type)
    type_condition = type_condition[:-4]
    return type_condition


def read_sql_table(table):
    print('Reading table {} from the database'.format(table))
    query = 'SELECT * FROM ' + table
    df = pd.read_sql(query, get_db_connection())
    return df

def get_project_id_by_url(project_url):
    query = "SELECT * FROM project WHERE url = '{}'".format(project_url)
    df = pd.read_sql(query, get_db_connection())
    return df.iloc[0]['id']

def get_project_name_by_id(project_id):
    query = "SELECT * FROM project WHERE id = '{}'".format(project_id)
    df = pd.read_sql(query, get_db_connection())
    return df.iloc[0]['name']

def get_project_by_id(project_id):
    query = "SELECT * FROM project WHERE id = '{}'".format(project_id)
    df = pd.read_sql(query, get_db_connection())
    return df.iloc[0]['url']

def get_refactoring_by_id(refactoring_id):
    query = "SELECT * FROM refactoring WHERE id = '{}'".format(refactoring_id)
    df = pd.read_sql(query, get_db_connection())
    return df.iloc[0]['refactoring_type']

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

def get_merge_commit(merge_commit_id):
    query = "SELECT * FROM merge_commit WHERE id = '{}'".format(merge_commit_id)
    df = pd.read_sql(query, get_db_connection())
    return df.iloc[0]

def get_projects():
    query = "SELECT * FROM project"
    df = pd.read_sql(query, get_db_connection())
    return df

def get_conflicting_merge_commits(project_id):
    query = "SELECT * FROM merge_commit WHERE is_conflicting = 1 AND project_id = '{}'".format(project_id)
    df = pd.read_sql(query, get_db_connection())
    return df

def get_merge_commits(project_id):
    query = "SELECT * FROM merge_commit WHERE project_id = '{}'".format(project_id)
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



def get_merge_commit_by_involved_refactorings():
    f = open('refMerge_evaluation_commits', 'w+')
    f.close()

    conflicting_region_histories = get_conflicting_region_histories()
    refactoring_regions = get_refactoring_regions()

    df = pd.DataFrame(columns=['commit_hash', 'file_path'])

    rr_grouped_by_project = refactoring_regions.groupby('project_id')
    involved_refactoring_counter = 0
    for project_id, project_crh in conflicting_region_histories.groupby('project_id'):

        if project_id in rr_grouped_by_project.groups:
            print('Processing project {}'.format(project_id))
            project_rrs = rr_grouped_by_project.get_group(project_id)
            crh_rr_combined = pd.merge(project_crh.reset_index(), project_rrs.reset_index(), on='commit_hash',
                                       how='inner')
            crh_with_involved_refs = crh_rr_combined[crh_rr_combined.apply(record_involved, axis=1)]

            mc_id_crh_involved = crh_with_involved_refs.groupby('merge_commit_id')
            for mc_id in mc_id_crh_involved.groups:
                files = []
                group = mc_id_crh_involved.get_group(mc_id)['path']
                for file in group:
                    if file not in files:
                        files.append(file)
                        mc_hash = get_merge_commit_hash(mc_id)
                        df = df.append({'commit_hash': mc_hash, 'file_path': file}, ignore_index=True)

            mc_with_involved_refs_count = len(crh_with_involved_refs.groupby('merge_commit_id'))
            involved_refactoring_counter = involved_refactoring_counter + mc_with_involved_refs_count
            s = str(get_project_by_id(project_id)) + " has " + str(mc_with_involved_refs_count) + " involved refactorings"
            print(s)
    print("total involved refactorings: {}".format(involved_refactoring_counter))
    return df


def sortDictionary(d):
    return sorted(d.items(), key=lambda item: item[1], reverse=True)


def print_refactorings_in_order(d):
    for k, v in sortDictionary(d):
        print(k, v)

def graph(d, title):
    sorted_d = dict(sortDictionary(d)[:10])
    fig, ax = plt.subplots()
    colors = []

    for k in sorted_d:
        if k in covered_types:
            colors.append('black')
        else:
            colors.append('grey')

    barlist = plt.bar(sorted_d.keys(), sorted_d.values(), color=colors)


#    plt.title(title)
    ax.set_xlabel("Refactoring Type")
    ax.set_ylabel("Total Refactorings")
    fig.autofmt_xdate()
    fig.tight_layout()
    plt.savefig(title + '.pdf')


def graph_refactoring_types():
    r_rt = get_refactorings().groupby('refactoring_type')
    d = {}
    d2 = {}

    for rt in r_rt.groups:
        d[rt] = len(r_rt.get_group(rt))

    refactoring_regions = get_refactoring_regions()
    conficting_regions = get_conflicting_regions()
    conflicting_region_histories = get_conflicting_region_histories()
    crs_with_involved_refs = pd.DataFrame()
    rr_grouped_by_project = refactoring_regions.groupby('project_id')

    counter = 0
    for project_id, project_crh in conflicting_region_histories.groupby('project_id'):
        counter += 1
        print('Processing project {}'.format(counter))
        project_rrs = rr_grouped_by_project.get_group(project_id)
        crh_rr_combined = pd.merge(project_crh.reset_index(), project_rrs.reset_index(), on='commit_hash', how='inner')
        print("Finding involved refactorings")
        crh_with_involved_refs = crh_rr_combined[crh_rr_combined.apply(record_involved, axis=1)]
        for refactoring_id in crh_with_involved_refs['refactoring_id']:
            refactoring = get_refactoring_by_id(refactoring_id)
            if refactoring not in d2:
                d2[refactoring] = 1
            else:
                d2[refactoring] += 1
    graph(d, "All refactorings in dataset")
    print_refactorings_in_order(d)
    print("============\n===========\n===========")
    graph(d2, "Involved refactorings in dataset")
    print_refactorings_in_order(d2)
    print("============\n===========\n===========")
    print_refactorings_in_order(d3)

def get_data_frame(df_name):
    try:
        return pd.read_pickle(df_name + '.pickle')
    except FileNotFoundError:
        df = getattr(sys.modules[__name__], 'get_' + df_name)()
        df.to_pickle(df_name + '.pickle')
        return df

if __name__ == '__main__':
    get_data_frame('merge_commit_by_involved_refactorings')
