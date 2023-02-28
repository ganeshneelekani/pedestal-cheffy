(ns cheffy.db.recipe
  (:require [next.jdbc.sql :as sql]
            [next.jdbc :as jdbc]))

(defn list-all-recipes
  [db account-id]
  (with-open [conn (jdbc/get-connection db)]
    (let [public (sql/find-by-keys conn :recipe {:public true})]
      (if account-id
        (let [drafts (sql/find-by-keys conn :recipe
                                       {:public false :uid account-id})]
          {:public public
           :drafts drafts})
        {:public public}))))

(defn insert-recipe!
  [db recipe]
  (sql/insert! db :recipe
               (assoc recipe :public false
                      :favorite-count 0)
               jdbc/snake-kebab-opts))

(defn find-recipe-by-id
  [db recipe-id]
  (with-open [conn (jdbc/get-connection db)]
    (let [[recipe] (sql/find-by-keys conn :recipe {:recipe_id recipe-id})
          steps (sql/find-by-keys conn :step {:recipe_id recipe-id})
          ingredeints (sql/find-by-keys conn :ingredient {:recipe_id recipe-id})]
      (when (seq recipe)
        (assoc recipe
               :recipe/steps steps
               :recipe/ingredients ingredeints)))))

(defn delete-recipe!
  [conn recipe]
  (-> (sql/delete! conn :recipe recipe)
      :next.jdbc/update-count
      (pos?)))