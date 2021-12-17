import pandas as pd
from sqlalchemy import create_engine
import matplotlib.pyplot as plt
import numpy as np
import sys
import re
from scipy import stats
from scipy.stats import ranksums
from tabulate import tabulate
import math
import statistics

supported_types = ['RENAME_CLASS', 'MOVE_CLASS', 'MOVE_RENAME_CLASS', 'RENAME_OPERATION', 'MOVE_OPERATION', 'MOVE_AND_RENAME_OPERATION', 'EXTRACT_OPERATION', 'INLINE_OPERATION']

unsupported_types = ['CHANGE_PACKAGE', 'MOVE_ATTRIBUTE', 'RENAME_ATTRIBUTE', 'PULL_UP_OPERATION', 'PUSH_DOWN_OPERATION', 'ADD_PARAMETER', 'CHANGE_PARAMETER_TYPE', 'RENAME_PACKAGE', 'RENAME_PARAMETER', 'PUSH_DOWN_ATTRIBUTE']


def get_db_connection():
    username = 'root'
    password = "password"
    database_name = 'refMerge_evaluation'
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
    if is_source or is_dest:
        project_url = get_project_url(x['merge_commit_id'])
        merge_commit_hash = get_merge_commit_hash(x['merge_commit_id'])
        write_to_csv(project_url, merge_commit_hash)
    return is_source or is_dest


accepted_types = ['Change Package', 'Extract And Move Method', 'Extract Interface', 'Extract Method',
                      'Extract Superclass', 'Inline Method', 'Move And Rename Class', 'Move Attribute', 'Move Class',
                      'Move Method', 'Pull Up Attribute', 'Pull Up Method', 'Pull Up Method', 'Push Down Method',
                      'Rename Class', 'Rename Method']

covered_types = ['Extract Method', 'Inline Method', 'Rename Method', 'Rename Class', 'Move Method', 'Move Class', 'Move And Rename Method', 'Move And Rename Class']



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
    with open('refMerge_evaluation_commits', 'a') as f:
        f.write(entry + '\n')

def get_refactoring_types_sql_condition():
    type_condition = str()
    for ref_type in unsupported_types:
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

def get_merge_results():
    return read_sql_table('merge_result')


def get_project_by_id(project_id):
    query = "SELECT * FROM project WHERE id = '{}'".format(project_id)
    df = pd.read_sql(query, get_db_connection())
    return df.iloc[0]['name']

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

def get_merge_commit_id_from_commit_hash(commit_hash):
    query = "SELECT * FROM merge_commit WHERE commit_hash = '{}'".format(commit_hash)
    df = pd.read_sql(query, get_db_connection())
    return df.iloc[0]['id']

def get_projects():
    query = "SELECT * FROM project"
    df = pd.read_sql(query, get_db_connection())
    return df

def get_conflicting_merge_commits(project_id):
    query = "SELECT * FROM merge_commit WHERE is_conflicting = 1 AND project_id = '{}'".format(project_id)
    df = pd.read_sql(query, get_db_connection())
    return df

def get_conflict_blocks():
    return read_sql_table('conflict_block') 


def get_conflicting_files():
    return read_sql_table('conflicting_file')

def get_supported_merge_commits():
    query = "SELECT * FROM merge_commit WHERE id not in (SELECT merge_commit_id FROM refactoring WHERE ({}))"\
            .format(get_refactoring_types_sql_condition())
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

def mc_has_supported_types(mc_id):
    query = "SELECT distinct refactoring_type FROM refactoring WHERE merge_commit_id = '{}'".format(mc_id)
    df = pd.read_sql(query, get_db_connection())
    for row in df.iloc:
        if row['refactoring_type'] in supported_types:
            return True
    return False

def get_refactorings():
    return read_sql_table('refactoring')

def get_refactoring_regions():
    return read_sql_table('refactoring_region')


def get_accepted_refactoring_regions():
    print('Reading table refactoring_region from the database')

    query = 'select * from refactoring_region where refactoring_id in (select id from refactoring where ({}))'\
        .format(get_refactoring_types_sql_condition())
    return pd.read_sql(query, get_db_connection())

def get_statistics_per_scenario():

    merge_results = get_merge_results()
    counter = 0

    plot_df = pd.DataFrame(columns=['project_id', 'merge_commit_id', 'merge_tool', 'precision', 'recall', 'conflict_blocks', 'conflicting_loc'])
    mr_grouped_by_mc = merge_results.groupby('merge_commit_id')
    merge_results_df = pd.DataFrame(merge_results)

    for _, mr in merge_results_df.groupby('id'):
        plot_df = plot_df.append({'project_id': mr['project_id'], 'merge_commit_id': mr['merge_commit_id'], 'merge_tool': str(mr['merge_tool'].iloc[0]), 'precision': mr['auto_merged_precision'].iloc[0], 'recall': mr['auto_merged_recall'].iloc[0], 'conflict_blocks': mr['total_conflicts'], 'conflicting_loc': mr['total_conflicting_loc'].iloc[0]}, ignore_index=True)
        
    return plot_df

def get_conflicting_loc_per_block_with_comments_stats():
    merge_commits = get_merge_commits()
    conflict_blocks = get_conflict_blocks()
    counter = 0
    cb_mc = conflict_blocks.groupby('merge_commit_id')
    merge_results_grouped = get_merge_results().groupby('merge_commit_id')
    plot_df = pd.DataFrame(columns=['project_name', 'refMerge_conflicting_loc', 'intelliMerge_conflicting_loc', 'git_conflicting_loc'])

    for project_id, project_mc in merge_commits.groupby('project_id'):
        refMerge_total = 0
        intelliMerge_total = 0
        git_total = 0
        count = 0
        for mc_cb in project_mc.iloc:
            count += 1
            if mc_cb['is_done'] == 0:
                continue

            mc_mr = merge_results_grouped.get_group(mc_cb['id'])
            skip = []
            for mr in mc_mr.iloc:
                runtime = mr['runtime']
                if int(runtime) == 18000000 or int(runtime) == -1:
                    skip.append(mr['merge_tool'])
            if len(skip) == 2:
                continue

            try:
                cb_group = cb_mc.get_group(mc_cb['id'])
            except:
                continue
            refMerge_conflicting_loc = cb_group[(cb_group['merge_tool'] == 'RefMerge')]['conflicting_loc'].tolist()
            intelliMerge_conflicting_loc = cb_group[(cb_group['merge_tool'] == 'IntelliMerge')]['conflicting_loc'].tolist()
            git_conflicting_loc = cb_group[(cb_group['merge_tool'] == 'Git')]['conflicting_loc'].tolist()

            for loc in refMerge_conflicting_loc:
                if 'RefMerge' in skip:
                    break
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflicting_loc': loc, 'intelliMerge_conflicting_loc': -1, 'git_conflicting_loc': -1}, ignore_index = True)
            if len(refMerge_conflicting_loc) == 0 and 'RefMerge' not in skip:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflicting_loc': 0, 'intelliMerge_conflicting_loc': -1, 'git_conflicting_loc': -1}, ignore_index = True)

            for loc in intelliMerge_conflicting_loc:
                if 'IntelliMerge' in skip:
                    break
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflicting_loc': -1, 'intelliMerge_conflicting_loc': loc, 'git_conflicting_loc': -1}, ignore_index = True)
            if len(intelliMerge_conflicting_loc) == 0 and 'RefMerge' not in skip:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflicting_loc': -1, 'intelliMerge_conflicting_loc': 0, 'git_conflicting_loc': -1}, ignore_index = True)

            for loc in git_conflicting_loc:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflicting_loc': -1, 'intelliMerge_conflicting_loc': -1, 'git_conflicting_loc': loc}, ignore_index = True)

    return plot_df


def get_conflicting_loc_with_comments_stats():
    merge_commits = get_merge_commits()
    conflict_blocks = get_conflict_blocks()
    counter = 0
    cb_mc = conflict_blocks.groupby('merge_commit_id')
    merge_results_grouped = get_merge_results().groupby('merge_commit_id')
    plot_df = pd.DataFrame(columns=['project_name', 'refMerge_conflicting_loc', 'intelliMerge_conflicting_loc', 'git_conflicting_loc'])
    for project_id, project_mc in merge_commits.groupby('project_id'):
        refMerge_total = 0
        intelliMerge_total = 0
        git_total = 0
        count = 0
        for mc_cb in project_mc.iloc:
            count += 1
            if mc_cb['is_done'] == 0:
                continue

            mc_mr = merge_results_grouped.get_group(mc_cb['id'])
            skip = [] 
            for mr in mc_mr.iloc:
                runtime = mr['runtime']
                if int(runtime) == 18000000 or int(runtime) == -1:
                    skip.append(mr['merge_tool'])
            if len(skip) == 2:
                continue

            try:
                cb_group = cb_mc.get_group(mc_cb['id'])
            except:
                continue
            refMerge_conflicting_loc = sum(cb_group[(cb_group['merge_tool'] == 'RefMerge')]['conflicting_loc'].tolist())
            intelliMerge_conflicting_loc = sum(cb_group[(cb_group['merge_tool'] == 'IntelliMerge')]['conflicting_loc'].tolist())
            git_conflicting_loc = sum(cb_group[(cb_group['merge_tool'] == 'Git')]['conflicting_loc'].tolist())

            refMerge_reduction = (git_conflicting_loc - refMerge_conflicting_loc) * 100 / git_conflicting_loc
            intelliMerge_reduction = (git_conflicting_loc - intelliMerge_conflicting_loc) * 100 / git_conflicting_loc
            if 'RefMerge' in skip:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflicting_loc': -1, 'intelliMerge_conflicting_loc': intelliMerge_conflicting_loc, 'git_conflicting_loc': git_conflicting_loc}, ignore_index = True)

            elif 'IntelliMerge' in skip:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflicting_loc': refMerge_conflicting_loc, 'intelliMerge_conflicting_loc': -1, 'git_conflicting_loc': git_conflicting_loc}, ignore_index = True)

            else:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflicting_loc': refMerge_conflicting_loc, 'intelliMerge_conflicting_loc': intelliMerge_conflicting_loc, 'git_conflicting_loc': git_conflicting_loc}, ignore_index = True)

            refMerge_total += refMerge_reduction
            intelliMerge_total += intelliMerge_reduction

        refMerge_total = refMerge_total / count
        intelliMerge_total = intelliMerge_total / count
        print(project_id, " RefMerge: ", refMerge_total)
        print(project_id, " IntelliMerge: ", intelliMerge_total)

    return plot_df


def get_conflicting_loc_stats():
    merge_commits = get_merge_commits()
    conflict_blocks = get_conflict_blocks()
    counter = 0
    cb_mc = conflict_blocks.groupby('merge_commit_id')
    merge_results_grouped = get_merge_results().groupby('merge_commit_id')
    plot_df = pd.DataFrame(columns=['project_name', 'refMerge_conflicting_loc', 'intelliMerge_conflicting_loc', 'git_conflicting_loc'])
    for project_id, project_mc in merge_commits.groupby('project_id'):
        refMerge_total = 0
        intelliMerge_total = 0
        git_total = 0
        count = 0
        for mc_cb in project_mc.iloc:
            count += 1
            if mc_cb['is_done'] == 0:
                continue

            mc_mr = merge_results_grouped.get_group(mc_cb['id'])
            skip = [] 
            for mr in mc_mr.iloc:
                runtime = mr['runtime']
                if int(runtime) == 18000000 or int(runtime) == -1:
                    skip.append(mr['merge_tool'])
            if len(skip) == 2:
                continue


            cb_group = cb_mc.get_group(mc_cb['id']) 
            refMerge_conflicting_loc = sum(cb_group[(cb_group['merge_tool'] == 'RefMerge') & (cb_group['is_comment'] == 0)]['conflicting_loc'].tolist())
            intelliMerge_conflicting_loc = sum(cb_group[(cb_group['merge_tool'] == 'IntelliMerge') & (cb_group['is_comment'] == 0)]['conflicting_loc'].tolist())
            git_conflicting_loc = sum(cb_group[(cb_group['merge_tool'] == 'Git') & (cb_group['is_comment'] == 0)]['conflicting_loc'].tolist())
            if git_conflicting_loc == 0:
                continue

            refMerge_reduction = (git_conflicting_loc - refMerge_conflicting_loc) * 100 / git_conflicting_loc
            intelliMerge_reduction = (git_conflicting_loc - intelliMerge_conflicting_loc) * 100 / git_conflicting_loc

            if 'RefMerge' in skip:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflicting_loc': -1, 'intelliMerge_conflicting_loc': intelliMerge_conflicting_loc, 'git_conflicting_loc': git_conflicting_loc}, ignore_index = True)

            elif 'IntelliMerge' in skip:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflicting_loc': refMerge_conflicting_loc, 'intelliMerge_conflicting_loc': -1, 'git_conflicting_loc': git_conflicting_loc}, ignore_index = True)

            else:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflicting_loc': refMerge_conflicting_loc, 'intelliMerge_conflicting_loc': intelliMerge_conflicting_loc, 'git_conflicting_loc': git_conflicting_loc}, ignore_index = True)



            refMerge_total += refMerge_reduction
            intelliMerge_total += intelliMerge_reduction

        refMerge_total = refMerge_total / count
        intelliMerge_total = intelliMerge_total / count
        print(project_id, " RefMerge: ", refMerge_total)
        print(project_id, " IntelliMerge: ", intelliMerge_total)

    return plot_df

def get_conflicting_scenarios():
    merge_commits = get_merge_commits()
    conflict_blocks = get_conflict_blocks()
    counter = 0
    cb_mc = conflict_blocks.groupby('merge_commit_id')
    plot_df = pd.DataFrame(columns=['project_name', 'refMerge_conflicts', 'refMerge_timeouts', 'refMerge_resolved', 'intelliMerge_conflicts', 'intelliMerge_timeouts', 'intelliMerge_resolved' 'git_conflicts', 'refMerge_comments_conflicts', 'git_comments_conflicts', 'intelliMerge_comments_conflicts'])
    merge_results_grouped = get_merge_results().groupby('merge_commit_id')
    refMerge_final = 0
    intelliMerge_final = 0
    for project_id, project_mc in merge_commits.groupby('project_id'):
        refMerge_comments_total = 0
        intelliMerge_comments_total = 0
        refMerge_total = 0
        intelliMerge_total = 0
        refMerge_resolved = 0
        intelliMerge_resolved = 0
        refMerge_timeouts = 0
        intelliMerge_timeouts = 0
        git_total = 0
        git_comments_total = 0
        count = 0
        for mc_cb in project_mc.iloc:
            if mc_cb['is_done'] == 0:
                continue
            mc_mr = merge_results_grouped.get_group(mc_cb['id'])

            skip = []
            for mr in mc_mr.iloc:
                runtime = mr['runtime']
                if int(runtime) == 18000000 or int(runtime) == -1:
                    skip.append(mr['merge_tool'])

            if len(skip) == 2:
                refMerge_conflict_blocks_comments = 0
                refMerge_conflict_blocks = 0
                intelliMerge_conflict_blocks_comments = 0
                intelliMerge_conflict_blocks = 0
                git_conflict_blocks = 1
            else:
                cb_group = cb_mc.get_group(mc_cb['id']) 
                refMerge_conflict_blocks_comments = len(cb_group[(cb_group['merge_tool'] == 'RefMerge')]['id'].tolist())
                intelliMerge_conflict_blocks_comments = len(cb_group[(cb_group['merge_tool'] == 'IntelliMerge')]['id'].tolist())
                refMerge_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'RefMerge') & (cb_group['is_comment'] == 0)]['id'].tolist())
                intelliMerge_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'IntelliMerge') & (cb_group['is_comment'] == 0)]['id'].tolist())
                git_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'Git') & (cb_group['is_comment'] == 0)]['id'].tolist())
            if 'RefMerge' in skip:
                refMerge_timeouts += 1
            elif refMerge_conflict_blocks != 0:
                refMerge_total += 1
            elif refMerge_conflict_blocks_comments != 0:
                refMerge_comments_total += 1
            else:
                refMerge_resolved += 1

            if 'IntelliMerge' in skip:
                intelliMerge_timeouts += 1
            elif intelliMerge_conflict_blocks != 0:
                intelliMerge_total += 1
            elif intelliMerge_conflict_blocks_comments != 0:
                intelliMerge_comments_total += 1
            else: 
                intelliMerge_resolved += 1

            if git_conflict_blocks != 0:
                git_total += 1
            else:
                git_comments_total += 1
            count += 1
        plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflicts': refMerge_total, 'refMerge_timeouts': refMerge_timeouts, 'refMerge_resolved': refMerge_resolved, 'intelliMerge_conflicts': intelliMerge_total, 'intelliMerge_timeouts': intelliMerge_timeouts, 'intelliMerge_resolved': intelliMerge_resolved, 'git_conflicts': git_total, 'refMerge_comments_conflicts': refMerge_comments_total, 'intelliMerge_comments_conflicts': intelliMerge_comments_total, 'git_comments_conflicts': git_comments_total}, ignore_index = True)
        
    return plot_df

def get_conflicting_scenarios_with_comments():
    merge_commits = get_merge_commits()
    conflict_blocks = get_conflict_blocks()
    counter = 0
    cb_mc = conflict_blocks.groupby('merge_commit_id')
    plot_df = pd.DataFrame(columns=['project_name', 'refMerge', 'intelliMerge'])
    merge_results_grouped = get_merge_results().groupby('merge_commit_id')
    refMerge_final = 0
    intelliMerge_final = 0
    for project_id, project_mc in merge_commits.groupby('project_id'):
        refMerge_total = 0
        intelliMerge_total = 0
        git_total = 0
        count = 0
        for mc_cb in project_mc.iloc:
            if mc_cb['is_done'] == 0:
                continue
            mc_mr = merge_results_grouped.get_group(mc_cb['id'])
            skip = []
            for mr in mc_mr.iloc:
                runtime = mr['runtime']
                if int(runtime) == 18000000 or int(runtime) == -1:
                    skip.append(mr['merge_tool'])
            cb_group = cb_mc.get_group(mc_cb['id']) 
            refMerge_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'RefMerge') & (cb_group['is_comment'] == 0)]['id'].tolist())
            intelliMerge_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'IntelliMerge') & (cb_group['is_comment'] == 0)]['id'].tolist())
            git_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'Git') & (cb_group['is_comment'] == 0)]['id'].tolist())
            if git_conflict_blocks == 0:
                continue
            if 'RefMerge' in skip or refMerge_conflict_blocks != 0:
                refMerge_total += 1

            if 'IntelliMerge' in skip or intelliMerge_conflict_blocks != 0:
                intelliMerge_total += 1
            count += 1

        refMerge_reduction = (count - refMerge_total) * 100 / count
        intelliMerge_reduction = (count - intelliMerge_total) * 100 / count
        plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge': refMerge_reduction, 'intelliMerge': intelliMerge_reduction}, ignore_index = True)
        refMerge_final += refMerge_total
        intelliMerge_final += intelliMerge_total
        print(refMerge_final)
        print(intelliMerge_final)
        
    return plot_df

def get_discrepancies_between_tools():
    merge_commits = get_merge_commits()
    merge_results = get_merge_results()
    df = pd.DataFrame(columns=['project_id', 'merge_commit_id', 'r_files', 'g_files', 'i_files', 'r_blocks', 'g_blocks', 'i_blocks', 'r_loc', 'g_loc', 'i_loc', 'r_runtime', 'i_runtime'])

    for project_id in merge_results['project_id'].unique():
        project_data = merge_results[merge_results.project_id == project_id]
        unique_merge_scenarios = project_data['merge_commit_id'].unique()

        for merge_scenario in unique_merge_scenarios:

            r_files = project_data.loc[(project_data.merge_tool=="RefMerge") & (project_data.merge_commit_id == merge_scenario), 'total_conflicting_files'].values[0]
            i_files = project_data.loc[(project_data.merge_tool=="IntelliMerge") & (project_data.merge_commit_id == merge_scenario), 'total_conflicting_files'].values[0]
            g_files = project_data.loc[(project_data.merge_tool=="Git") & (project_data.merge_commit_id == merge_scenario), 'total_conflicting_files'].values[0]

            g_blocks = project_data.loc[(project_data.merge_tool=="Git") & (project_data.merge_commit_id == merge_scenario), 'total_conflicts'].values[0]
            i_blocks = project_data.loc[(project_data.merge_tool=="IntelliMerge") & (project_data.merge_commit_id == merge_scenario), 'total_conflicts'].values[0]
            r_blocks = project_data.loc[(project_data.merge_tool=="RefMerge") & (project_data.merge_commit_id == merge_scenario), 'total_conflicts'].values[0]

            g_loc = project_data.loc[(project_data.merge_tool=="Git") & (project_data.merge_commit_id == merge_scenario), 'total_conflicting_loc'].values[0]
            i_loc = project_data.loc[(project_data.merge_tool=="IntelliMerge") & (project_data.merge_commit_id == merge_scenario), 'total_conflicting_loc'].values[0]
            r_loc = project_data.loc[(project_data.merge_tool=="RefMerge") & (project_data.merge_commit_id == merge_scenario), 'total_conflicting_loc'].values[0]

            r_runtime = project_data.loc[(project_data.merge_tool=="RefMerge") & (project_data.merge_commit_id == merge_scenario), 'runtime'].values[0]
            i_runtime = project_data.loc[(project_data.merge_tool=="IntelliMerge") & (project_data.merge_commit_id == merge_scenario), 'runtime'].values[0]

            df = df.append({'project_id': project_id, 'merge_commit_id': merge_scenario,
                'r_files': r_files, 'i_files': i_files, 'g_files': g_files, 'r_blocks': r_blocks,
                'i_blocks': i_blocks, 'g_blocks': g_blocks, 'r_loc': r_loc, 'i_loc': i_loc, 'g_loc': g_loc,
                'r_runtime': r_runtime, 'i_runtime': i_runtime}, ignore_index=True)
    return df

def get_sampling_scenarios():
    df = get_data_frame('discrepancies_between_tools')

    both_resolve = []
    both_reduce = []
    both_increase = []

    intelliMerge_better_resolves = []
    intelliMerge_better_files = []
    intelliMerge_better_blocks = []
    intelliMerge_better_loc =[]

    refMerge_better_resolves = []
    refMerge_better_files = []
    refMerge_better_blocks = []
    refMerge_better_loc = []
    for _, row in df.iterrows():
        if row['i_runtime'] == "-1" or row['r_runtime'] == "-1":
            continue

        if row['i_blocks'] == row['r_blocks'] and row['i_blocks'] > row['g_blocks']:
            both_increase.append(row)
        elif row['i_blocks'] == row['r_blocks'] and row['i_blocks'] == 0:
            both_resolve.append(row)
        elif row['i_files'] == row['r_files'] and row['i_blocks'] == row['r_blocks'] and row['i_blocks'] < row['g_blocks']:
            both_reduce.append(row)
        elif row['i_blocks'] < row['r_blocks'] and row['i_blocks'] == 0:
            intelliMerge_better_resolves.append(row)
        elif row['i_files'] < row['r_files'] and row['i_files'] < row['g_files']:
            intelliMerge_better_files.append(row)
        elif row['i_blocks'] < row['r_blocks'] and row['i_blocks'] < row['g_blocks']:
            intelliMerge_better_blocks.append(row)
        elif row['i_loc'] < row['r_loc'] and row['i_loc'] < row['g_loc']:
            intelliMerge_better_loc.append(row)
        elif row['i_blocks'] > row['r_blocks'] and row['r_blocks'] == 0:
            refMerge_better_resolves.append(row)
        elif row['i_files'] > row['r_files'] and row['r_files'] < row['g_files']:
            refMerge_better_files.append(row)
        elif row['i_blocks'] > row['r_blocks'] and row['r_blocks'] < row['g_blocks']:
            refMerge_better_blocks.append(row)
        elif row['i_loc'] > row['r_loc'] and row['r_loc'] < row['g_loc']:
            refMerge_better_loc.append(row)

    project_ids = [0] * 20
    merge_commit_ids = []

    get_random_scenarios(both_resolve, project_ids, 3, merge_commit_ids)
    get_random_scenarios(both_reduce, project_ids, 3, merge_commit_ids)
    get_random_scenarios(both_increase, project_ids, 5, merge_commit_ids)

    get_random_scenarios(intelliMerge_better_resolves, project_ids, 5, merge_commit_ids)
    get_random_scenarios(intelliMerge_better_files, project_ids, 5, merge_commit_ids)
    get_random_scenarios(intelliMerge_better_blocks, project_ids, 5, merge_commit_ids)
    get_random_scenarios(intelliMerge_better_loc, project_ids, 5, merge_commit_ids)

    get_random_scenarios(refMerge_better_loc, project_ids, 5, merge_commit_ids)
    get_random_scenarios(refMerge_better_files, project_ids, 5, merge_commit_ids)
    get_random_scenarios(refMerge_better_blocks, project_ids, 5, merge_commit_ids)
    get_random_scenarios(refMerge_better_resolves, project_ids, 5, merge_commit_ids)

    print(merge_commit_ids)
    merge_commit_ids.sort()
    print(merge_commit_ids)

def get_random_scenarios(list, project_ids, num, merge_commit_ids):
    np.random.seed(338218)
    # Get the projects in each bin
    print("L " + str(len(list)))
    rows = []
    i = 0
    ids_in_list = []
    for row in list:
        project_id = row['project_id']
        if project_id not in ids_in_list:
            if project_ids[project_id - 1] == 0:
                ids_in_list.append(row['project_id'])



    print(ids_in_list)
    j = 0
    while i < num:
        if j == len(list):
            j = 0
            for row in list:
                project_id = row['project_id']
                if project_id not in ids_in_list:
                    if project_ids[project_id - 1] == 1:
                        print(ids_in_list)
                        ids_in_list.append(row['project_id'])
            if len(ids_in_list) == 0:
                for row in list:
                    project_id = row['project_id']
                    if project_id not in ids_in_list:
                        if project_ids[project_id - 1] == 2:
                            print(ids_in_list)
                            ids_in_list.append(row['project_id'])

        row = round(np.random.random() * (len(list) - 1))
        if row not in rows:
            project_id = list[row]['project_id']
            merge_commit_id = list[row]['merge_commit_id']
            if project_id in ids_in_list:
                print(project_id)
                print(project_ids)
                print(ids_in_list)
                if project_ids[project_id - 1] < 3 and merge_commit_id not in merge_commit_ids:
                    ids_in_list.remove(project_id)
                    rows.append(row)
                    project_ids[project_id - 1] += 1
                    merge_commit_ids.append(merge_commit_id)
                    i += 1
        j += 1


def get_conflict_block_with_comments_stats():
    merge_commits = get_merge_commits()
    conflict_blocks = get_conflict_blocks()
    counter = 0
    cb_mc = conflict_blocks.groupby('merge_commit_id')
    plot_df = pd.DataFrame(columns=['project_name', 'refMerge_conflict_blocks', 'intelliMerge_conflict_blocks', 'git_conflict_blocks'])
    merge_results_grouped = get_merge_results().groupby('merge_commit_id')
    for project_id, project_mc in merge_commits.groupby('project_id'):
        refMerge_total = 0
        intelliMerge_total = 0
        git_total = 0
        count = 0
        for mc_cb in project_mc.iloc:
            count += 1
            if mc_cb['is_done'] == 0:
                continue
            mc_mr = merge_results_grouped.get_group(mc_cb['id'])
            skip = []
            for mr in mc_mr.iloc:
                runtime = mr['runtime']
                if int(runtime) == 18000000 or int(runtime) == -1:
                    skip.append(mr['merge_tool'])
            if len(skip) == 2:
                continue

            try:
                cb_group = cb_mc.get_group(mc_cb['id'])
            except:
                continue
            refMerge_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'RefMerge')]['id'].tolist())
            intelliMerge_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'IntelliMerge')]['id'].tolist())
            git_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'Git')]['id'].tolist())

            refMerge_reduction = (git_conflict_blocks - refMerge_conflict_blocks) * 100 / git_conflict_blocks
            intelliMerge_reduction = (git_conflict_blocks - intelliMerge_conflict_blocks) * 100 / git_conflict_blocks
            if 'RefMerge' in skip:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflict_blocks': -1, 'intelliMerge_conflict_blocks': intelliMerge_conflict_blocks, 'git_conflict_blocks': git_conflict_blocks}, ignore_index = True)

            elif 'IntelliMerge' in skip:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflict_blocks': refMerge_conflict_blocks, 'intelliMerge_conflict_blocks': -1, 'git_conflict_blocks': git_conflict_blocks}, ignore_index = True)

            else:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflict_blocks': refMerge_conflict_blocks, 'intelliMerge_conflict_blocks': intelliMerge_conflict_blocks, 'git_conflict_blocks': git_conflict_blocks}, ignore_index = True)

            refMerge_total += refMerge_reduction
            intelliMerge_total += intelliMerge_reduction

        refMerge_total = refMerge_total / count
        intelliMerge_total = intelliMerge_total / count
        print(project_id, " RefMerge: ", refMerge_total)
        print(project_id, " IntelliMerge: ", intelliMerge_total)

    return plot_df

def get_conflict_block_stats():
    merge_commits = get_merge_commits()
    conflict_blocks = get_conflict_blocks()
    counter = 0
    cb_mc = conflict_blocks.groupby('merge_commit_id')
    plot_df = pd.DataFrame(columns=['project_name', 'refMerge_conflict_blocks', 'intelliMerge_conflict_blocks', 'git_conflict_blocks'])
    merge_results_grouped = get_merge_results().groupby('merge_commit_id')
    for project_id, project_mc in merge_commits.groupby('project_id'):
        refMerge_total = 0
        intelliMerge_total = 0
        git_total = 0
        count = 0
        
        
        for mc_cb in project_mc.iloc:
            count += 1
            if mc_cb['is_done'] == 0:
                continue;

            mc_mr = merge_results_grouped.get_group(mc_cb['id'])
            skip = []
            for mr in mc_mr.iloc:
                runtime = mr['runtime']
                if int(runtime) == 18000000 or int(runtime) == -1:
                    skip.append(mr['merge_tool'])
            if len(skip) == 2:
                continue


            cb_group = cb_mc.get_group(mc_cb['id'])
            refMerge_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'RefMerge') & (cb_group['is_comment'] == 0)]['id'].tolist())
            intelliMerge_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'IntelliMerge') & (cb_group['is_comment'] == 0)]['id'].tolist())
            git_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'Git') & (cb_group['is_comment'] == 0)]['id'].tolist())
            if git_conflict_blocks == 0:
                continue

            refMerge_reduction = (git_conflict_blocks - refMerge_conflict_blocks) * 100 / git_conflict_blocks
            intelliMerge_reduction = (git_conflict_blocks - intelliMerge_conflict_blocks) * 100 / git_conflict_blocks

            if 'RefMerge' in skip:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflict_blocks': -1, 'intelliMerge_conflict_blocks': intelliMerge_conflict_blocks, 'git_conflict_blocks': git_conflict_blocks}, ignore_index = True)

            elif 'IntelliMerge' in skip:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflict_blocks': refMerge_conflict_blocks, 'intelliMerge_conflict_blocks': -1, 'git_conflict_blocks': git_conflict_blocks}, ignore_index = True)

            else:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge_conflict_blocks': refMerge_conflict_blocks, 'intelliMerge_conflict_blocks': intelliMerge_conflict_blocks, 'git_conflict_blocks': git_conflict_blocks}, ignore_index = True)




            refMerge_total += refMerge_reduction
            intelliMerge_total += intelliMerge_reduction

    return plot_df


def get_conflicting_scenario_stats():
    merge_commits = get_merge_commits()
    conflict_blocks = get_conflict_blocks()
    counter = 0
    cb_mc = conflict_blocks.groupby('merge_commit_id')
    plot_df = pd.DataFrame(columns=['project_name', 'refMerge', 'intelliMerge', 'git'])
    merge_results_grouped = get_merge_results().groupby('merge_commit_id')
    for project_id, project_mc in merge_commits.groupby('project_id'):
        refMerge_total = 0
        intelliMerge_total = 0
        git_total = 0
        count = 0
        
        
        for mc_cb in project_mc.iloc:
            count += 1
            if mc_cb['is_done'] == 0:
                continue
            mc_mr = merge_results_grouped.get_group(mc_cb['id'])
            skip = 0
            for mr in mc_mr.iloc:
                runtime = mr['runtime']
                if int(runtime) == 18000000 or int(runtime) == -1:
                    skip += 1
            if skip == 2:
                continue

            cb_group = cb_mc.get_group(mc_cb['id'])

            refMerge_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'RefMerge') & (cb_group['is_comment'] == 0)]['id'].tolist())
            intelliMerge_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'IntelliMerge') & (cb_group['is_comment'] == 0)]['id'].tolist())
            git_conflict_blocks = len(cb_group[(cb_group['merge_tool'] == 'Git') & (cb_group['is_comment'] == 0)]['id'].tolist())

            if refMerge_conflict_blocks > 0:
                refMerge_conflict_blocks = 1
            if intelliMerge_conflict_blocks > 0:
                intelliMerge_conflict_blocks = 1
            if git_conflict_blocks > 0:
                git_conflict_blocks = 1

            plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge': refMerge_conflict_blocks, 'intelliMerge': intelliMerge_conflict_blocks, 'git': git_conflict_blocks}, ignore_index = True)


    return plot_df

def get_conflicting_file_with_comments_stats():
    merge_commits = get_merge_commits()
    conflict_blocks = get_conflict_blocks()
    counter = 0
    cb_mc = conflict_blocks.groupby('merge_commit_id')
    merge_results_grouped = get_merge_results().groupby('merge_commit_id')
    plot_df = pd.DataFrame(columns=['project_name', 'refMerge', 'git', 'intelliMerge'])
    for project_id, project_mc in merge_commits.groupby('project_id'):
        refMerge_total = 0
        intelliMerge_total = 0
        count = 0
        for mc_cb in project_mc.iloc:
            count += 1
            if mc_cb['is_done'] == 0:
                continue;
            skip = []
            mc_mr = merge_results_grouped.get_group(mc_cb['id'])
            for mr in mc_mr.iloc:
                runtime = mr['runtime']
                if int(runtime) == 18000000 or int(runtime) == -1:
                    skip.append(mr['merge_tool'])
            if len(skip) == 2:
                continue

            try:
                cb_group = cb_mc.get_group(mc_cb['id'])
            except:
                continue
            refMerge_conflicting_files = len(pd.unique(cb_group[(cb_group['merge_tool'] == 'RefMerge')]['conflicting_file_id'].tolist()))
            intelliMerge_conflicting_files = len(pd.unique(cb_group[(cb_group['merge_tool'] == 'IntelliMerge')]['conflicting_file_id'].tolist()))
            git_conflicting_files = len(pd.unique(cb_group[(cb_group['merge_tool'] == 'Git')]['conflicting_file_id'].tolist()))


            if 'RefMerge' in skip:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge': -1, 'git': git_conflicting_files, 'intelliMerge': intelliMerge_conflicting_files,}, ignore_index=True)

            elif 'IntelliMerge' in skip:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge': refMerge_conflicting_files, 'git': git_conflicting_files, 'intelliMerge': -1}, ignore_index=True)

            else:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge': refMerge_conflicting_files, 'git': git_conflicting_files, 'intelliMerge': intelliMerge_conflicting_files,}, ignore_index=True)

            


    return plot_df

def get_conflicting_file_stats():
    merge_commits = get_merge_commits()
    conflict_blocks = get_conflict_blocks()
    counter = 0
    cb_mc = conflict_blocks.groupby('merge_commit_id')
    plot_df = pd.DataFrame(columns=['project_name', 'refMerge', 'git', 'intelliMerge'])
    merge_results_grouped = get_merge_results().groupby('merge_commit_id')
    for project_id, project_mc in merge_commits.groupby('project_id'):
        refMerge_total = 0
        intelliMerge_total = 0
        count = 0
        for mc_cb in project_mc.iloc:
            count += 1
            if mc_cb['is_done'] == 0:
                continue;

            mc_mr = merge_results_grouped.get_group(mc_cb['id'])
            skip = []
            for mr in mc_mr.iloc:
                runtime = mr['runtime']
                if int(runtime) == 18000000 or int(runtime) == -1:
                    skip.append(mr['merge_tool'])
            if len(skip) == 2:
                continue


            cb_group = cb_mc.get_group(mc_cb['id'])
            refMerge_conflicting_files = len(pd.unique(cb_group[(cb_group['merge_tool'] == 'RefMerge') & (cb_group['is_comment'] == 0)]['conflicting_file_id'].tolist()))
            intelliMerge_conflicting_files = len(pd.unique(cb_group[(cb_group['merge_tool'] == 'IntelliMerge') & (cb_group['is_comment'] == 0)]['conflicting_file_id'].tolist()))
            git_conflicting_files = len(pd.unique(cb_group[(cb_group['merge_tool'] == 'Git') & (cb_group['is_comment'] == 0)]['conflicting_file_id'].tolist()))

            if 'RefMerge' in skip:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge': -1, 'git': git_conflicting_files, 'intelliMerge': intelliMerge_conflicting_files,}, ignore_index=True)

            elif 'IntelliMerge' in skip:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge': refMerge_conflicting_files, 'git': git_conflicting_files, 'intelliMerge': -1}, ignore_index=True)

            else:
                plot_df = plot_df.append({'project_name': get_project_by_id(project_id), 'refMerge': refMerge_conflicting_files, 'git': git_conflicting_files, 'intelliMerge': intelliMerge_conflicting_files,}, ignore_index=True)


            


    return plot_df


def get_detailed_scenario_stats_per_project():
    merge_results = get_merge_results()

    df = pd.DataFrame(columns=['project_name', 'total_scenarios', 'total_resolvedIntelliMerge', 'timedOut_intelliMerge', 'total_conflictingIntelliMerge', 'total_resolvedRefMerge', 'timedOut_refMerge', 'total_conflictingRefMerge'])


    all_intelliMerge_resolved = 0
    all_intelliMerge_timeouts = 0
    all_intelliMerge_same = 0
    all_refMerge_resolved = 0
    all_refMerge_timeouts = 0
    all_refMerge_same = 0

    all_scenarios = 0

    for project_id in merge_results['project_id'].unique():
        project_name = get_project_by_id(project_id)
        project_data = merge_results[merge_results.project_id == project_id]
        unique_merge_scenarios = project_data['merge_commit_id'].unique()

        total_scenarios = len(unique_merge_scenarios)
        all_scenarios += total_scenarios
        total_resolved_intelliMerge = len(project_data[(project_data.merge_tool=="IntelliMerge") & (project_data.total_conflicts == 0) & (project_data.runtime != "-1")])
        total_resolved_refMerge = len(project_data[(project_data.merge_tool=="RefMerge") & (project_data.total_conflicts == 0) & (project_data.runtime != "-1")])

        total_timedOut_intelliMerge = len(project_data[(project_data.merge_tool=="IntelliMerge") & (project_data.runtime == "-1")])
        total_timedOut_refMerge = len(project_data[(project_data.merge_tool=="RefMerge") & (project_data.runtime == "-1")])

        total_conflicting_intelliMerge = total_scenarios - total_resolved_intelliMerge - total_timedOut_intelliMerge
        total_conflicting_refMerge = total_scenarios - total_resolved_refMerge - total_timedOut_refMerge

        all_intelliMerge_resolved += total_resolved_intelliMerge
        all_refMerge_resolved += total_resolved_refMerge

        all_intelliMerge_timeouts += total_timedOut_intelliMerge
        all_refMerge_timeouts += total_timedOut_refMerge

        all_intelliMerge_same += total_conflicting_intelliMerge
        all_refMerge_same += total_conflicting_refMerge

        same_refmerge_git = 0
        same_intellimerge_git = 0

        df = df.append({'project_name':project_name, 'total_scenarios':total_scenarios,
            'total_resolvedIntelliMerge':total_resolved_intelliMerge,
            'timedOut_intelliMerge': total_timedOut_intelliMerge,
            'total_conflictingIntelliMerge': total_conflicting_intelliMerge,
            'total_resolvedRefMerge':total_resolved_refMerge,
            'timedOut_refMerge': total_timedOut_refMerge,
            'total_conflictingRefMerge': total_conflicting_refMerge}, ignore_index=True)

    df = df.append({'project_name':'all', 'total_scenarios':all_scenarios,
            'total_resolvedIntelliMerge':all_intelliMerge_resolved,
            'total_conflictingIntelliMerge': all_intelliMerge_same,
            'timedOut_intelliMerge': all_intelliMerge_timeouts,
            'total_resolvedRefMerge':all_refMerge_resolved,
            'timedOut_refMerge': all_refMerge_timeouts,
            'total_conflictingRefMerge': all_refMerge_same}, ignore_index=True)
    df.to_csv("../results/overall_scenarios.csv")


def get_detailed_file_stats_per_project():
    merge_results = get_merge_results()

    df = pd.DataFrame(columns=['project_name', 'total_scenarios', 'reduced_conflicting_files_intelliMerge', 'increased_conflicting_files_intelliMerge', 'same_conflicting_files_intelliMerge', 'reduced_conflicting_files_refMerge', 'increased_conflicting_files_refMerge', 'same_conflicting_files_refMerge',])

    all_intelliMerge_reductions = []
    all_intelliMerge_increases = []
    all_refMerge_reductions = []
    all_refMerge_increases = []
    all_scenarios = 0

    for project_id in merge_results['project_id'].unique():
        project_name = get_project_by_id(project_id)
        project_data = merge_results[merge_results.project_id == project_id]
        unique_merge_scenarios = project_data['merge_commit_id'].unique()

        intelliMerge_reductions = []
        intelliMerge_increases = []
        refMerge_reductions = []
        refMerge_increases = []

        total_scenarios = len(unique_merge_scenarios)
        all_scenarios += total_scenarios

        reduced_intelliMerge_git = 0
        increased_intelliMerge_git = 0
        same_intelliMerge_git = 0

        reduced_refMerge_git = 0
        increased_refMerge_git = 0
        same_refMerge_git = 0

        median_intelliMerge_reduction = 0
        median_refMerge_reduction = 0
        median_intelliMerge_increase = 0
        median_refMerge_increase = 0

        for merge_scenario in unique_merge_scenarios:
            git_conflicts = project_data.loc[(project_data.merge_tool=="Git") & (project_data.merge_commit_id == merge_scenario), 'total_conflicting_files'].values[0]
            intelliMerge_conflicts = project_data.loc[(project_data.merge_tool=="IntelliMerge") & (project_data.merge_commit_id == merge_scenario), 'total_conflicting_files'].values[0]
            refMerge_conflicts = project_data.loc[(project_data.merge_tool=="RefMerge") & (project_data.merge_commit_id == merge_scenario) , 'total_conflicting_files'].values[0]

            if intelliMerge_conflicts > 0: #completely resolved are already accounted for
                if git_conflicts == intelliMerge_conflicts:
                    same_intelliMerge_git += 1
                elif intelliMerge_conflicts < git_conflicts:
                    reduced_intelliMerge_git +=1
                    intelliMerge_reductions.append((git_conflicts - intelliMerge_conflicts)/git_conflicts)
                else:
                    increased_intelliMerge_git += 1
                    intelliMerge_increases.append((intelliMerge_conflicts - git_conflicts)/git_conflicts)

            if refMerge_conflicts > 0: #completely resolved are already accounted for
                if git_conflicts == refMerge_conflicts:
                    same_refMerge_git += 1
                elif refMerge_conflicts < git_conflicts:
                    reduced_refMerge_git +=1
                    refMerge_reductions.append((git_conflicts - refMerge_conflicts)/git_conflicts)
                else:
                    increased_refMerge_git += 1
                    refMerge_increases.append((refMerge_conflicts - git_conflicts)/git_conflicts)


        all_intelliMerge_reductions.extend(intelliMerge_reductions)
        all_intelliMerge_increases.extend(intelliMerge_increases)
        all_refMerge_reductions.extend(refMerge_reductions)
        all_refMerge_increases.extend(refMerge_increases)

        if(len(intelliMerge_reductions) > 0):
            median_intelliMerge_reduction = statistics.median(intelliMerge_reductions)

        if(len(intelliMerge_increases) > 0):
            median_intelliMerge_increase = statistics.median(intelliMerge_increases)

        if(len(refMerge_reductions) > 0):
            median_refMerge_reduction = statistics.median(refMerge_reductions)

        if(len(refMerge_increases) > 0):
            median_refMerge_increase = statistics.median(refMerge_increases)

        df = df.append({'project_name':project_name, 'total_scenarios':total_scenarios,
            'reduced_conflicting_files_intelliMerge': str(reduced_intelliMerge_git) + " (" + "{:.0%}".format(median_intelliMerge_reduction) + ")",
            'increased_conflicting_files_intelliMerge': str(increased_intelliMerge_git) + " (" + "{:.0%}".format(median_intelliMerge_increase) + ")",
            'same_conflicting_files_intelliMerge':same_intelliMerge_git,
            'reduced_conflicting_files_refMerge':str(reduced_refMerge_git) + " (" + "{:.0%}".format(median_refMerge_reduction) + ")",
            'increased_conflicting_files_refMerge':str(increased_refMerge_git) + " (" + "{:.0%}".format(median_refMerge_increase) + ")",
            'same_conflicting_files_refMerge':same_refMerge_git}, ignore_index=True)


    df = df.append({'project_name':'all', 'total_scenarios':all_scenarios,
            'reduced_conflicting_files_intelliMerge': str(len(all_intelliMerge_reductions)) + " (" + "{:.0%}".format(statistics.median(all_intelliMerge_reductions)) + ")",
            'increased_conflicting_files_intelliMerge': str(len(all_intelliMerge_increases)) + " (" + "{:.0%}".format(statistics.median(all_intelliMerge_increases)) + ")",
            'same_conflicting_files_intelliMerge':-1,
            'reduced_conflicting_files_refMerge':str(len(all_refMerge_reductions)) + " (" + "{:.0%}".format(statistics.median(all_refMerge_reductions)) + ")",
            'increased_conflicting_files_refMerge':str(len(all_refMerge_increases)) + " (" + "{:.0%}".format(statistics.median(all_refMerge_increases)) + ")",
            'same_conflicting_files_refMerge':-1}, ignore_index=True)
    df.to_csv("../results/overall_files.csv")


def get_detailed_block_stats_per_project():
    merge_results = get_merge_results()

    df = pd.DataFrame(columns=['project_name', 'total_scenarios', 'reduced_conflict_intelliMerge', 'increased_conflict_intelliMerge', 'same_conflict_intelliMerge', 'reduced_conflict_refMerge', 'increased_conflict_refMerge', 'same_conflict_refMerge',])

    all_intelliMerge_reductions = []
    all_intelliMerge_increases = []
    all_refMerge_reductions = []
    all_refMerge_increases = []
    all_scenarios = 0

    for project_id in merge_results['project_id'].unique():
        project_name = get_project_by_id(project_id)
        project_data = merge_results[merge_results.project_id == project_id]
        unique_merge_scenarios = project_data['merge_commit_id'].unique()

        intelliMerge_reductions = []
        intelliMerge_increase = []
        refMerge_reductions = []
        refMerge_increase = []

        total_scenarios = len(unique_merge_scenarios)
        all_scenarios += total_scenarios

        reduced_intelliMerge_git = 0
        increased_intelliMerge_git = 0
        same_intelliMerge_git = 0

        reduced_refMerge_git = 0
        increased_refMerge_git = 0
        same_refMerge_git = 0

        median_intelliMerge_reduction = 0
        median_refMerge_reduction = 0
        median_intelliMerge_increase = 0
        median_refMerge_increase = 0

        for merge_scenario in unique_merge_scenarios:
            git_conflicts = project_data.loc[(project_data.merge_tool=="Git") & (project_data.merge_commit_id == merge_scenario), 'total_conflicts'].values[0]
            intelliMerge_conflicts = project_data.loc[(project_data.merge_tool=="IntelliMerge") & (project_data.merge_commit_id == merge_scenario), 'total_conflicts'].values[0]
            refMerge_conflicts = project_data.loc[(project_data.merge_tool=="RefMerge") & (project_data.merge_commit_id == merge_scenario), 'total_conflicts'].values[0]

            if intelliMerge_conflicts > 0: #completely resolved are already accounted for
                if git_conflicts == intelliMerge_conflicts:
                    same_intelliMerge_git += 1
                elif intelliMerge_conflicts < git_conflicts:
                    reduced_intelliMerge_git +=1
                    intelliMerge_reductions.append((git_conflicts - intelliMerge_conflicts)/git_conflicts)
                else:
                    increased_intelliMerge_git += 1
                    intelliMerge_increase.append((intelliMerge_conflicts - git_conflicts)/git_conflicts)

            if refMerge_conflicts > 0: #completely resolved are already accounted for
                if git_conflicts == refMerge_conflicts:
                    same_refMerge_git += 1
                elif refMerge_conflicts < git_conflicts:
                    reduced_refMerge_git +=1
                    refMerge_reductions.append((git_conflicts - refMerge_conflicts)/git_conflicts)
                else:
                    increased_refMerge_git += 1
                    refMerge_increase.append((refMerge_conflicts - git_conflicts)/git_conflicts)


        all_intelliMerge_reductions.extend(intelliMerge_reductions)
        all_intelliMerge_increases.extend(intelliMerge_increase)
        all_refMerge_reductions.extend(refMerge_reductions)
        all_refMerge_increases.extend(refMerge_increase)

        if(len(intelliMerge_reductions) > 0):
            median_intelliMerge_reduction = statistics.median(intelliMerge_reductions)

        if(len(intelliMerge_increase) > 0):
            median_intelliMerge_increase = statistics.median(intelliMerge_increase)

        if(len(refMerge_reductions) > 0):
            median_refMerge_reduction = statistics.median(refMerge_reductions)

        if(len(refMerge_increase) > 0):
            median_refMerge_increase = statistics.median(refMerge_increase)

        df = df.append({'project_name':project_name, 'total_scenarios':total_scenarios,
            'reduced_conflict_intelliMerge': str(reduced_intelliMerge_git) + " (" + "{:.0%}".format(median_intelliMerge_reduction) + ")",
            'increased_conflict_intelliMerge': str(increased_intelliMerge_git) + " (" + "{:.0%}".format(median_intelliMerge_increase) + ")",
            'same_conflict_intelliMerge':same_intelliMerge_git,
            'reduced_conflict_refMerge':str(reduced_refMerge_git) + " (" + "{:.0%}".format(median_refMerge_reduction) + ")",
            'increased_conflict_refMerge':str(increased_refMerge_git) + " (" + "{:.0%}".format(median_refMerge_increase) + ")",
            'same_conflict_refMerge':same_refMerge_git}, ignore_index=True)

    df = df.append({'project_name':'all', 'total_scenarios':all_scenarios,
            'reduced_conflict_intelliMerge': str(len(all_intelliMerge_reductions)) + " (" + "{:.0%}".format(statistics.median(all_intelliMerge_reductions)) + ")",
            'increased_conflict_intelliMerge': str(len(all_intelliMerge_increases)) + " (" + "{:.0%}".format(statistics.median(all_intelliMerge_increases)) + ")",
            'same_conflict_intelliMerge':-1,
            'reduced_conflict_refMerge':str(len(all_refMerge_reductions)) + " (" + "{:.0%}".format(statistics.median(all_refMerge_reductions)) + ")",
            'increased_conflict_refMerge':str(len(all_refMerge_increases)) + " (" + "{:.0%}".format(statistics.median(all_refMerge_increases)) + ")",
            'same_conflict_refMerge':-1}, ignore_index=True)
    df.to_csv("../results/overall_blocks.csv")

def get_detailed_loc_stats_per_project():
    merge_results = get_merge_results()

    df = pd.DataFrame(columns=['project_name', 'total_scenarios', 'reduced_conflicting_loc_intelliMerge', 'increased_conflicting_loc_intelliMerge', 'same_conflict_intelliMerge', 'reduced_conflicting_loc_refMerge', 'increased_conflicting_loc_refMerge', 'same_conflict_refMerge',])

    counter = 0

    all_intelliMerge_reductions = []
    all_intelliMerge_increases = []
    all_refMerge_reductions = []
    all_refMerge_increases = []
    all_scenarios = 0

    for project_id in merge_results['project_id'].unique():
        project_name = get_project_by_id(project_id)
        project_data = merge_results[merge_results.project_id == project_id]
        unique_merge_scenarios = project_data['merge_commit_id'].unique()

        intelliMerge_reductions = []
        intelliMerge_increase = []
        refMerge_reductions = []
        refMerge_increase = []

        total_scenarios = len(unique_merge_scenarios)
        all_scenarios += total_scenarios

        reduced_intelliMerge_git = 0
        increased_intelliMerge_git = 0
        same_intelliMerge_git = 0

        reduced_refMerge_git = 0
        increased_refMerge_git = 0
        same_refMerge_git = 0

        median_intelliMerge_reduction = 0
        median_refMerge_reduction = 0
        median_intelliMerge_increase = 0
        median_refMerge_increase = 0

        for merge_scenario in unique_merge_scenarios:
            git_conflicts = project_data.loc[(project_data.merge_tool=="Git") & (project_data.merge_commit_id == merge_scenario), 'total_conflicting_loc'].values[0]
            intelliMerge_conflicts = project_data.loc[(project_data.merge_tool=="IntelliMerge") & (project_data.merge_commit_id == merge_scenario), 'total_conflicting_loc'].values[0]
            refMerge_conflicts = project_data.loc[(project_data.merge_tool=="RefMerge") & (project_data.merge_commit_id == merge_scenario), 'total_conflicting_loc'].values[0]

            if intelliMerge_conflicts > 0: #completely resolved are already accounted for
                if git_conflicts == intelliMerge_conflicts:
                    same_intelliMerge_git += 1
                elif intelliMerge_conflicts < git_conflicts:
                    reduced_intelliMerge_git +=1
                    intelliMerge_reductions.append((git_conflicts - intelliMerge_conflicts)/git_conflicts)
                else:
                    increased_intelliMerge_git += 1
                    intelliMerge_increase.append((intelliMerge_conflicts - git_conflicts)/git_conflicts)

            if refMerge_conflicts > 0: #completely resolved are already accounted for
                if git_conflicts == refMerge_conflicts:
                    same_refMerge_git += 1
                elif refMerge_conflicts < git_conflicts:
                    reduced_refMerge_git +=1
                    refMerge_reductions.append((git_conflicts - refMerge_conflicts)/git_conflicts)
                else:
                    increased_refMerge_git += 1
                    refMerge_increase.append((refMerge_conflicts - git_conflicts)/git_conflicts)


        all_intelliMerge_reductions.extend(intelliMerge_reductions)
        all_intelliMerge_increases.extend(intelliMerge_increase)
        all_refMerge_reductions.extend(refMerge_reductions)
        all_refMerge_increases.extend(refMerge_increase)

        if(len(intelliMerge_reductions) > 0):
            median_intelliMerge_reduction = statistics.median(intelliMerge_reductions)

        if(len(intelliMerge_increase) > 0):
            median_intelliMerge_increase = statistics.median(intelliMerge_increase)

        if(len(refMerge_reductions) > 0):
            median_refMerge_reduction = statistics.median(refMerge_reductions)

        if(len(refMerge_increase) > 0):
            median_refMerge_increase = statistics.median(refMerge_increase)

        df = df.append({'project_name':project_name, 'total_scenarios':total_scenarios,
            'reduced_conflicting_loc_intelliMerge': str(reduced_intelliMerge_git) + " (" + "{:.0%}".format(median_intelliMerge_reduction) + ")",
            'increased_conflicting_loc_intelliMerge': str(increased_intelliMerge_git) + " (" + "{:.0%}".format(median_intelliMerge_increase) + ")",
            'same_conflict_intelliMerge':same_intelliMerge_git,
            'reduced_conflicting_loc_refMerge':str(reduced_refMerge_git) + " (" + "{:.0%}".format(median_refMerge_reduction) + ")",
            'increased_conflicting_loc_refMerge':str(increased_refMerge_git) + " (" + "{:.0%}".format(median_refMerge_increase) + ")",
            'same_conflict_refMerge':same_refMerge_git}, ignore_index=True)

    df = df.append({'project_name':'all', 'total_scenarios':all_scenarios,
            'reduced_conflicting_loc_intelliMerge': str(len(all_intelliMerge_reductions)) + " (" + "{:.0%}".format(statistics.median(all_intelliMerge_reductions)) + ")",
            'increased_conflicting_loc_intelliMerge': str(len(all_intelliMerge_increases)) + " (" + "{:.0%}".format(statistics.median(all_intelliMerge_increases)) + ")",
            'same_conflict_intelliMerge':-1,
            'reduced_conflicting_loc_refMerge':str(len(all_refMerge_reductions)) + " (" + "{:.0%}".format(statistics.median(all_refMerge_reductions)) + ")",
            'increased_conflicting_loc_refMerge':str(len(all_refMerge_increases)) + " (" + "{:.0%}".format(statistics.median(all_refMerge_increases)) + ")",
            'same_conflict_refMerge':-1}, ignore_index=True)
    df.to_csv("../results/overall_loc.csv")



def get_detailed_involved_blocks_stats_per_project(involved_df):
    conflict_blocks = get_conflict_blocks()

    conflict_blocks_grouped_by_mc = conflict_blocks.groupby('merge_commit_id')

    df = pd.DataFrame(columns=['project_name', 'total_scenarios', 'reduced_conflicting_files_intelliMerge', 'increased_conflicting_files_intelliMerge', 'same_conflicting_files_intelliMerge', 'reduced_conflicting_files_refMerge', 'increased_conflicting_files_refMerge', 'same_conflicting_files_refMerge',])

    all_intelliMerge_reductions = []
    all_intelliMerge_increases = []
    all_refMerge_reductions = []
    all_refMerge_increases = []
    all_scenarios = 0

    for project_id in conflict_blocks['project_id'].unique():
        project_name = get_project_by_id(project_id)

        conflicts_refMerge = 0
        conflicts_intelliMerge = 0
        conflicts_git = 0

        intelliMerge_reductions = []
        intelliMerge_increases = []
        refMerge_reductions = []
        refMerge_increases = []

        reduced_intelliMerge_git = 0
        increased_intelliMerge_git = 0
        same_intelliMerge_git = 0

        reduced_refMerge_git = 0
        increased_refMerge_git = 0
        same_refMerge_git = 0

        median_intelliMerge_reduction = 0
        median_refMerge_reduction = 0
        median_intelliMerge_increase = 0
        median_refMerge_increase = 0

        files_by_commit = involved_df.groupby('commit_hash')


        for commit_hash in involved_df['commit_hash']:
            mc_id = get_merge_commit_id_from_commit_hash(commit_hash)
            files = files_by_commit.get_group(commit_hash)

            conflict_blocks_grouped = conflict_blocks_grouped_by_mc.get_group(mc_id)

            for conflict_block in conflict_blocks_grouped:
                file = conflict_block['path']
                if file not in files:
                    continue

                merge_tool = conflict_block['merge_tool']

                if merge_tool == "RefMerge":
                    refMerge_conflicts += 1

                elif merge_tool == "IntelliMerge":
                    intelliMerge_conflicts += 1

                elif merge_tool == "Git":
                    git_conflicts += 1

            if intelliMerge_conflicts > 0: #completely resolved are already accounted for
                if git_conflicts == intelliMerge_conflicts:
                    same_intelliMerge_git += 1
                elif intelliMerge_conflicts < git_conflicts:
                    reduced_intelliMerge_git +=1
                    intelliMerge_reductions.append((git_conflicts - intelliMerge_conflicts)/git_conflicts)
                else:
                    increased_intelliMerge_git += 1
                    intelliMerge_increases.append((intelliMerge_conflicts - git_conflicts)/git_conflicts)

            if refMerge_conflicts > 0: #completely resolved are already accounted for
                if git_conflicts == refMerge_conflicts:
                    same_refMerge_git += 1
                elif refMerge_conflicts < git_conflicts:
                    reduced_refMerge_git +=1
                    refMerge_reductions.append((git_conflicts - refMerge_conflicts)/git_conflicts)
                else:
                    increased_refMerge_git += 1
                    refMerge_increases.append((refMerge_conflicts - git_conflicts)/git_conflicts)


        all_intelliMerge_reductions.extend(intelliMerge_reductions)
        all_intelliMerge_increases.extend(intelliMerge_increases)
        all_refMerge_reductions.extend(refMerge_reductions)
        all_refMerge_increases.extend(refMerge_increases)

        if(len(intelliMerge_reductions) > 0):
            median_intelliMerge_reduction = statistics.median(intelliMerge_reductions)

        if(len(intelliMerge_increases) > 0):
            median_intelliMerge_increase = statistics.median(intelliMerge_increases)

        if(len(refMerge_reductions) > 0):
            median_refMerge_reduction = statistics.median(refMerge_reductions)

        if(len(refMerge_increases) > 0):
            median_refMerge_increase = statistics.median(refMerge_increases)

        df = df.append({'project_name':project_name,
            'reduced_conflicting_files_intelliMerge': str(reduced_intelliMerge_git) + " (" + "{:.0%}".format(median_intelliMerge_reduction) + ")",
            'increased_conflicting_files_intelliMerge': str(increased_intelliMerge_git) + " (" + "{:.0%}".format(median_intelliMerge_increase) + ")",
            'same_conflicting_files_intelliMerge':same_intelliMerge_git,
            'reduced_conflicting_files_refMerge':str(reduced_refMerge_git) + " (" + "{:.0%}".format(median_refMerge_reduction) + ")",
            'increased_conflicting_files_refMerge':str(increased_refMerge_git) + " (" + "{:.0%}".format(median_refMerge_increase) + ")",
            'same_conflicting_files_refMerge':same_refMerge_git}, ignore_index=True)


    df = df.append({'project_name':'all',
            'reduced_conflicting_files_intelliMerge': str(len(all_intelliMerge_reductions)) + " (" + "{:.0%}".format(statistics.median(all_intelliMerge_reductions)) + ")",
            'increased_conflicting_files_intelliMerge': str(len(all_intelliMerge_increases)) + " (" + "{:.0%}".format(statistics.median(all_intelliMerge_increases)) + ")",
            'same_conflicting_files_intelliMerge':-1,
            'reduced_conflicting_files_refMerge':str(len(all_refMerge_reductions)) + " (" + "{:.0%}".format(statistics.median(all_refMerge_reductions)) + ")",
            'increased_conflicting_files_refMerge':str(len(all_refMerge_increases)) + " (" + "{:.0%}".format(statistics.median(all_refMerge_increases)) + ")",
            'same_conflicting_files_refMerge':-1}, ignore_index=True)
    df.to_csv("../results/overall_blocks_involved.csv")


def get_detailed_stats_per_project():
    get_detailed_scenario_stats_per_project()
    get_detailed_file_stats_per_project()
    get_detailed_block_stats_per_project()
    get_detailed_loc_stats_per_project()

def get_stats_for_files_with_refactoring_conflicts():
    df = get_data_frame('merge_commit_by_involved_refactorings')

    get_detailed_involved_blocks_stats_per_project(df)


def get_data_frame(df_name):
    try:
        return pd.read_pickle(df_name + '.pickle')
    except FileNotFoundError:
        df = getattr(sys.modules[__name__], 'get_' + df_name)()
        df.to_pickle(df_name + '.pickle')
        return df

def print_stats():
    df = get_data_frame('conflict_block_stats')
    df = get_data_frame('conflicting_file_stats')
    df = get_data_frame('conflicting_stats')


if __name__ == '__main__':
    get_sampling_scenarios()
    get_detailed_stats_per_project()

