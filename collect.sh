#!/bin/bash

if [ _COLLECTION_ ];
  then rm -R _COLLECTION_
fi

mkdir _COLLECTION_

cp SPIDER/SPIDERFile/target/*.jar _COLLECTION_
cp SPIDER/SPIDERDatabase/target/*.jar _COLLECTION_

cp BINDER/BINDERFile/target/*.jar _COLLECTION_
cp BINDER/BINDERDatabase/target/*.jar _COLLECTION_

cp MANY/target/*.jar _COLLECTION_

cp tane/tane_algorithm/target/*.jar _COLLECTION_
#cp tane/tane_tree_dir_algorithm/target/*.jar _COLLECTION_
#cp tane/tane_tree_end_algorithm/target/*.jar _COLLECTION_

cp fun/fun_for_metanome/target/*.jar _COLLECTION_

cp fdmine/fdmine_algorithm/target/*.jar _COLLECTION_

cp depminer/depminer_algorithm/target/*.jar _COLLECTION_

cp fastfds/fastfds_algorithm/target/*.jar _COLLECTION_

cp fdep/fdep_algorithm/target/*.jar _COLLECTION_
#cp fdep/fdep_algorithm_improved/target/*.jar _COLLECTION_

cp ducc/ducc_for_metanome/target/*.jar _COLLECTION_

cp dfd/dfdMetanome/target/*.jar _COLLECTION_

cp ORDER/target/*.jar _COLLECTION_

cp AIDFD/target/*.jar _COLLECTION_

cp FAIDA/FAIDAAlgorithm/target/*.jar _COLLECTION_

