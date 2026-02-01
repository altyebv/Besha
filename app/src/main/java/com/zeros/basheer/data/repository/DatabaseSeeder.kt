package com.zeros.basheer.data.repository

import com.zeros.basheer.data.local.dao.*
import com.zeros.basheer.data.models.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseSeeder @Inject constructor(
    private val subjectDao: SubjectDao,
    private val unitDao: UnitDao,
    private val lessonDao: LessonDao,
    private val conceptDao: ConceptDao
) {

    /**
     * Check if database has been seeded
     */
    suspend fun isDatabaseSeeded(): Boolean {
        return subjectDao.getSubjectById("geography") != null
    }

    /**
     * Seed all initial data (call this once on first launch)
     */
    suspend fun seedInitialData() {
        if (isDatabaseSeeded()) {
            return // Already seeded
        }
        seedGeographyData()
    }

    /**
     * Seed Geography subject data
     */
    suspend fun seedGeographyData() {
        // Insert Geography subject
        subjectDao.insertSubject(
            Subject(
                id = "geography",
                name = "الجغرافيا",
                nameAr = "الجغرافيا",
                nameEn = "Geography",
                path = StudentPath.LITERARY,
                isMajor = false,
                order = 1
            )
        )

        // Insert units
        unitDao.insertUnits(
            listOf(
                Units(
                    id = "geo_unit_1",
                    subjectId = "geography",
                    title = "الوحدة الأولى: الجغرافيا الطبيعية",
                    order = 1,
                    description = "دراسة التضاريس والمناخ والموارد الطبيعية"
                ),
                Units(
                    id = "geo_unit_2",
                    subjectId = "geography",
                    title = "الوحدة الثانية: الجغرافيا البشرية",
                    order = 2,
                    description = "دراسة السكان والعمران والأنشطة الاقتصادية"
                ),
                Units(
                    id = "geo_unit_3",
                    subjectId = "geography",
                    title = "الوحدة الثالثة: خرائط ونظم المعلومات الجغرافية",
                    order = 3,
                    description = "مهارات قراءة الخرائط واستخدام التقنيات الحديثة"
                )
            )
        )

        // Insert sample lessons for Unit 1
        lessonDao.insertLessons(
            listOf(
                Lesson(
                    id = "geo_lesson_1_1",
                    unitId = "geo_unit_1",
                    title = "الدرس الأول: الموقع الجغرافي والفلكي",
                    content = """
                        # الموقع الجغرافي والفلكي
                        
                        ## المقدمة
                        يعتبر الموقع من أهم العوامل المؤثرة في خصائص أي منطقة جغرافية. ينقسم الموقع إلى نوعين رئيسيين:
                        
                        ## الموقع الفلكي
                        هو موقع المكان بالنسبة لخطوط الطول ودوائر العرض. يحدد هذا الموقع:
                        - المناخ السائد في المنطقة
                        - عدد ساعات النهار والليل
                        - فصول السنة
                        
                        ### خطوط الطول
                        - خطوط وهمية تمتد من القطب الشمالي إلى القطب الجنوبي
                        - يبلغ عددها 360 خط
                        - خط غرينتش هو خط الطول صفر
                        - تستخدم لتحديد الزمن والتوقيت
                        
                        ### دوائر العرض
                        - دوائر وهمية موازية لخط الاستواء
                        - يبلغ عددها 180 دائرة (90 شمالاً و90 جنوباً)
                        - خط الاستواء هو دائرة العرض صفر
                        - تستخدم لتحديد المناخ والأقاليم الحرارية
                        
                        ## الموقع الجغرافي (النسبي)
                        هو موقع المكان بالنسبة للمناطق المحيطة به من:
                        - اليابسة والماء
                        - الدول المجاورة
                        - طرق المواصلات
                        - الموارد الطبيعية
                        
                        ## أهمية دراسة الموقع
                        - فهم المناخ والطقس
                        - التخطيط الزراعي والاقتصادي
                        - تحديد العلاقات السياسية
                        - فهم التنوع الثقافي
                    """.trimIndent(),
                    order = 1,
                    estimatedMinutes = 20,
                    tags = "location,coordinates,longitude,latitude,climate"
                ),
                Lesson(
                    id = "geo_lesson_1_2",
                    unitId = "geo_unit_1",
                    title = "الدرس الثاني: التضاريس والأشكال الأرضية",
                    content = """
                        # التضاريس والأشكال الأرضية
                        
                        ## مقدمة
                        التضاريس هي الأشكال المختلفة لسطح الأرض من جبال وهضاب وسهول ووديان.
                        
                        ## أنواع التضاريس
                        
                        ### الجبال
                        - كتل صخرية مرتفعة عن سطح البحر
                        - تتكون بفعل الحركات التكتونية
                        - أعلى قمة في العالم: جبل إيفرست (8,849م)
                        
                        #### أنواع الجبال
                        1. **الجبال الالتوائية**: تكونت بفعل الضغط الأفقي (جبال الهيمالايا)
                        2. **الجبال الانكسارية**: تكونت بفعل الصدوع (جبال البحر الأحمر)
                        3. **الجبال البركانية**: تكونت بفعل البراكين (جبل كلمنجارو)
                        
                        ### الهضاب
                        - أراضي مرتفعة ومستوية نسبياً
                        - تنحدر بشدة من جانب واحد على الأقل
                        - مثال: هضبة التبت، الهضبة الإثيوبية
                        
                        ### السهول
                        - أراضي منبسطة قليلة الارتفاع
                        - أكثر المناطق صلاحية للزراعة
                        - أنواعها:
                          - سهول ساحلية (محاذية للبحار)
                          - سهول فيضية (على ضفاف الأنهار)
                          - سهول داخلية
                        
                        ### الوديان
                        - منخفضات بين المرتفعات
                        - تشكلت بفعل التعرية المائية
                        - مهمة للزراعة والاستيطان
                        
                        ## العوامل المؤثرة في تشكيل التضاريس
                        
                        ### عوامل باطنية (داخلية)
                        - الحركات التكتونية
                        - البراكين
                        - الزلازل
                        
                        ### عوامل خارجية
                        - التعرية المائية (الأنهار والأمطار)
                        - التعرية الهوائية (الرياح)
                        - التعرية الجليدية
                        - التجوية (تفتت الصخور)
                        
                        ## أهمية دراسة التضاريس
                        - التخطيط العمراني
                        - الزراعة والرعي
                        - استخراج المعادن
                        - السياحة
                        - طرق المواصلات
                    """.trimIndent(),
                    order = 2,
                    estimatedMinutes = 25,
                    tags = "terrain,mountains,plains,plateaus,landforms"
                ),
                Lesson(
                    id = "geo_lesson_1_3",
                    unitId = "geo_unit_1",
                    title = "الدرس الثالث: المناخ والطقس",
                    content = """
                        # المناخ والطقس
                        
                        ## الفرق بين المناخ والطقس
                        
                        ### الطقس
                        - حالة الجو في مكان معين ووقت قصير (يوم أو أسبوع)
                        - يتغير بسرعة
                        - مثال: "الطقس اليوم ممطر"
                        
                        ### المناخ
                        - معدل حالة الجو في منطقة لفترة طويلة (30 سنة أو أكثر)
                        - ثابت نسبياً
                        - مثال: "مناخ السودان صحراوي حار"
                        
                        ## عناصر المناخ
                        
                        ### 1. درجة الحرارة
                        - تقاس بالترمومتر
                        - تتأثر بـ:
                          - دوائر العرض (كلما اقتربنا من خط الاستواء ارتفعت الحرارة)
                          - الارتفاع عن سطح البحر (تنخفض درجة بمعدل 6.5°م لكل 1000م ارتفاع)
                          - القرب من المسطحات المائية
                          - التيارات البحرية
                        
                        ### 2. الضغط الجوي
                        - وزن عمود الهواء على نقطة معينة
                        - يقاس بالبارومتر
                        - يؤثر على حركة الرياح
                        
                        ### 3. الرياح
                        - حركة الهواء من الضغط المرتفع إلى المنخفض
                        - أنواعها:
                          - رياح دائمة (التجارية، العكسية، القطبية)
                          - رياح موسمية
                          - رياح محلية
                        
                        ### 4. الرطوبة والأمطار
                        - الرطوبة: كمية بخار الماء في الجو
                        - الأمطار تتكون عندما يتكثف بخار الماء
                        - أنواع الأمطار:
                          - تصاعدية (استوائية)
                          - تضاريسية (جبلية)
                          - إعصارية (جبهية)
                        
                        ## الأقاليم المناخية
                        
                        ### 1. الإقليم الاستوائي
                        - حار وممطر طوال العام
                        - أمطار غزيرة (أكثر من 2000 مم)
                        - غابات كثيفة
                        
                        ### 2. الإقليم المداري (السافانا)
                        - حار، أمطار صيفية
                        - فصل جاف طويل
                        - حشائش طويلة
                        
                        ### 3. الإقليم الصحراوي
                        - حار جاف، أمطار نادرة (أقل من 250 مم)
                        - تفاوت كبير في الحرارة
                        - نباتات شوكية نادرة
                        
                        ### 4. الإقليم المتوسطي
                        - حار جاف صيفاً، دافئ ممطر شتاءً
                        - أمطار شتوية (400-600 مم)
                        - غابات دائمة الخضرة
                        
                        ## التغير المناخي
                        - ارتفاع درجة حرارة الأرض
                        - ذوبان الجليد القطبي
                        - ارتفاع مستوى البحار
                        - تغير أنماط الأمطار
                        - زيادة الظواهر الجوية المتطرفة
                    """.trimIndent(),
                    order = 3,
                    estimatedMinutes = 30,
                    tags = "climate,weather,temperature,rainfall,wind"
                )
            )
        )

        // Insert sample concepts
        conceptDao.insertConcepts(
            listOf(
                Concept(
                    id = "geo_concept_longitude",
                    subjectId = "geography",
                    type = ConceptType.DEFINITION,
                    title = "خطوط الطول",
                    content = "خطوط وهمية تمتد من القطب الشمالي إلى القطب الجنوبي، عددها 360 خط، تستخدم لتحديد الزمن والتوقيت. خط غرينتش هو خط الطول صفر.",
                    tags = "coordinates,location,time",
                    difficulty = 2,
                    relatedLessonIds = "geo_lesson_1_1"
                ),
                Concept(
                    id = "geo_concept_latitude",
                    subjectId = "geography",
                    type = ConceptType.DEFINITION,
                    title = "دوائر العرض",
                    content = "دوائر وهمية موازية لخط الاستواء، عددها 180 دائرة (90 شمالاً و90 جنوباً)، تستخدم لتحديد المناخ والأقاليم الحرارية.",
                    tags = "coordinates,location,climate",
                    difficulty = 2,
                    relatedLessonIds = "geo_lesson_1_1"
                ),
                Concept(
                    id = "geo_concept_fold_mountains",
                    subjectId = "geography",
                    type = ConceptType.DEFINITION,
                    title = "الجبال الالتوائية",
                    content = "جبال تكونت بفعل الضغط الأفقي على القشرة الأرضية مما أدى إلى التوائها وارتفاعها. مثال: جبال الهيمالايا والألب.",
                    tags = "mountains,terrain,geology",
                    difficulty = 3,
                    relatedLessonIds = "geo_lesson_1_2"
                ),
                Concept(
                    id = "geo_concept_climate_vs_weather",
                    subjectId = "geography",
                    type = ConceptType.COMPARISON,
                    title = "الفرق بين المناخ والطقس",
                    content = "الطقس: حالة الجو في مكان ووقت قصير (يتغير بسرعة). المناخ: معدل حالة الجو لفترة طويلة (30 سنة أو أكثر، ثابت نسبياً).",
                    tags = "climate,weather,definitions",
                    difficulty = 1,
                    relatedLessonIds = "geo_lesson_1_3"
                ),
                Concept(
                    id = "geo_concept_equatorial_climate",
                    subjectId = "geography",
                    type = ConceptType.DEFINITION,
                    title = "الإقليم الاستوائي",
                    content = "إقليم مناخي يتميز بالحرارة والأمطار الغزيرة طوال العام (أكثر من 2000 مم)، توجد فيه غابات كثيفة دائمة الخضرة.",
                    tags = "climate,regions,rainforest",
                    difficulty = 2,
                    relatedLessonIds = "geo_lesson_1_3"
                )
            )
        )
    }

    /**
     * Clear all data (useful for testing)
     */
    suspend fun clearAllData() {
        subjectDao.deleteAllSubjects()
    }
}